import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, extractErrorMessage } from '@/lib/api';
import type { Booking, BookingSummary } from '@/types';

interface CreateBookingPayload {
  showId: number;
  rowNumber: number;
  seatNumber: number;
}

export function useMyBookings(sort?: 'price') {
  return useQuery({
    queryKey: ['bookings', 'me', sort ?? 'recent'],
    queryFn: async () => {
      const res = await api.get<Booking[]>('/api/v1/bookings/me', {
        params: sort ? { sort } : undefined,
      });
      return res.data;
    },
  });
}

export function useBookingSummary() {
  return useQuery({
    queryKey: ['bookings', 'me', 'summary'],
    queryFn: async () => {
      const res = await api.get<BookingSummary>('/api/v1/bookings/me/summary');
      return res.data;
    },
  });
}

export function useCreateBooking() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: CreateBookingPayload) => {
      const res = await api.post<Booking>('/api/v1/bookings', data);
      return res.data;
    },
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ['shows', vars.showId, 'seats'] });
      qc.invalidateQueries({ queryKey: ['shows'] });
      qc.invalidateQueries({ queryKey: ['bookings', 'me'] });
    },
  });
}

/**
 * Result of a multi-seat booking attempt. We surface both successes and failures
 * so the UI can tell the user "3 of 4 seats booked; row 2 seat 7 was just taken".
 */
export interface BulkBookingResult {
  succeeded: Booking[];
  failed: { rowNumber: number; seatNumber: number; reason: string }[];
}

/**
 * Books multiple seats in parallel. Each seat is a separate POST under the hood
 * (the backend has no bulk endpoint and we don't want to add one — keeps the
 * backend contract unchanged). Partial success is normal: another user may grab
 * one of the seats between our parallel requests.
 */
export function useCreateBookings() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: {
      showId: number;
      seats: { rowNumber: number; seatNumber: number }[];
    }): Promise<BulkBookingResult> => {
      const results = await Promise.allSettled(
        data.seats.map((s) =>
          api.post<Booking>('/api/v1/bookings', {
            showId: data.showId,
            rowNumber: s.rowNumber,
            seatNumber: s.seatNumber,
          })
        )
      );

      const succeeded: Booking[] = [];
      const failed: BulkBookingResult['failed'] = [];

      results.forEach((r, i) => {
        const seat = data.seats[i];
        if (r.status === 'fulfilled') {
          succeeded.push(r.value.data);
        } else {
          failed.push({
            rowNumber: seat.rowNumber,
            seatNumber: seat.seatNumber,
            reason: extractErrorMessage(r.reason),
          });
        }
      });

      return { succeeded, failed };
    },
    onSettled: (_data, _err, vars) => {
      qc.invalidateQueries({ queryKey: ['shows', vars.showId, 'seats'] });
      qc.invalidateQueries({ queryKey: ['shows'] });
      qc.invalidateQueries({ queryKey: ['bookings', 'me'] });
    },
  });
}

export function useCancelBooking() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (bookingId: number) => {
      const res = await api.delete<Booking>(`/api/v1/bookings/${bookingId}`);
      return res.data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['bookings', 'me'] });
      qc.invalidateQueries({ queryKey: ['shows'] });
    },
  });
}
