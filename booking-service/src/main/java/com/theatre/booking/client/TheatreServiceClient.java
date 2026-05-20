package com.theatre.booking.client;

import com.theatre.booking.client.dto.SeatBookingRequest;
import com.theatre.booking.client.dto.SeatInfo;
import com.theatre.booking.client.dto.SeatMapInfo;
import com.theatre.booking.config.TheatreServiceProperties;
import com.theatre.booking.exception.SeatUnavailableException;
import com.theatre.booking.exception.ShowNotFoundException;
import com.theatre.booking.exception.TheatreServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for theatre-service. Sends the shared internal-token header
 * so theatre-service recognizes us as a trusted internal caller.
 */
@Component
@Slf4j
public class TheatreServiceClient {

    public static final String INTERNAL_HEADER = "X-Internal-Token";

    private final RestClient restClient;
    private final String internalToken;

    public TheatreServiceClient(TheatreServiceProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();
        this.internalToken = props.internalToken();
    }

    /** Fetch seat map (public endpoint, no auth needed). */
    public SeatMapInfo getSeatMap(Long showId) {
        try {
            return restClient.get()
                    .uri("/api/v1/shows/{id}/seats", showId)
                    .retrieve()
                    .body(SeatMapInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ShowNotFoundException(showId);
        } catch (ResourceAccessException e) {
            throw new TheatreServiceException("Cannot reach theatre service: " + e.getMessage());
        }
    }

    /** Reserve a seat. Throws if already booked or invalid coordinates. */
    public SeatInfo bookSeat(Long showId, int row, int seat) {
        try {
            return restClient.post()
                    .uri("/api/v1/shows/{id}/seats/book", showId)
                    .header(INTERNAL_HEADER, internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SeatBookingRequest(row, seat))
                    .retrieve()
                    .body(SeatInfo.class);
        } catch (HttpClientErrorException e) {
            handleClientError(e, showId, row, seat);
            return null; // unreachable
        } catch (ResourceAccessException e) {
            throw new TheatreServiceException("Cannot reach theatre service: " + e.getMessage());
        }
    }

    /** Release a previously-booked seat (used on cancel). */
    public void releaseSeat(Long showId, int row, int seat) {
        try {
            restClient.post()
                    .uri("/api/v1/shows/{id}/seats/release", showId)
                    .header(INTERNAL_HEADER, internalToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new SeatBookingRequest(row, seat))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            // On release we log but don't fail — the booking row is what matters
            log.warn("Failed to release seat showId={} row={} seat={}: {}", showId, row, seat, e.getMessage());
        } catch (ResourceAccessException e) {
            log.warn("Theatre service unreachable on release: {}", e.getMessage());
        }
    }

    private void handleClientError(HttpClientErrorException e, Long showId, int row, int seat) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == HttpStatus.CONFLICT) {
            throw new SeatUnavailableException(row, seat);
        }
        if (status == HttpStatus.NOT_FOUND) {
            throw new ShowNotFoundException(showId);
        }
        if (status == HttpStatus.BAD_REQUEST) {
            throw new SeatUnavailableException(
                    "Invalid seat coordinates for row=" + row + " seat=" + seat);
        }
        throw new TheatreServiceException("Theatre service error: " + e.getStatusCode());
    }
}
