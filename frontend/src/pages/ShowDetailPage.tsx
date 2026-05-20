import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Calendar, MapPin, X } from 'lucide-react';
import { useShow, useSeatMap } from '@/hooks/useShows';
import { useCreateBookings, type BulkBookingResult } from '@/hooks/useBookings';
import { useAuthStore } from '@/store/auth';
import { Button } from '@/components/ui/Button';
import { Card, CardContent } from '@/components/ui/Card';
import { Alert } from '@/components/ui/Alert';
import { SeatPicker } from '@/components/seats/SeatPicker';
import { ShowPoster } from '@/components/shows/ShowPoster';
import { formatDate, formatPrice } from '@/lib/utils';
import { extractErrorMessage } from '@/lib/api';
import type { Seat } from '@/types';

export function ShowDetailPage() {
  const { id } = useParams<{ id: string }>();
  const showId = id ? Number(id) : undefined;
  const navigate = useNavigate();
  const isLoggedIn = useAuthStore((s) => s.token !== null);

  const showQ = useShow(showId);
  const seatMapQ = useSeatMap(showId);
  const createBookings = useCreateBookings();

  // Multi-select: array of seat objects rather than a single one
  const [selectedSeats, setSelectedSeats] = useState<Seat[]>([]);
  const [bulkResult, setBulkResult] = useState<BulkBookingResult | null>(null);

  if (!showId) {
    return <div className="p-8">Invalid show.</div>;
  }

  const totalPrice = selectedSeats.reduce((sum, s) => sum + parseFloat(s.price), 0);

  const toggleSeat = (seat: Seat) => {
    setBulkResult(null); // clear previous result on new selection
    setSelectedSeats((curr) => {
      const exists = curr.some(
        (s) => s.rowNumber === seat.rowNumber && s.seatNumber === seat.seatNumber
      );
      if (exists) {
        return curr.filter(
          (s) => !(s.rowNumber === seat.rowNumber && s.seatNumber === seat.seatNumber)
        );
      }
      return [...curr, seat];
    });
  };

  const handleBook = () => {
    if (selectedSeats.length === 0) return;
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }
    createBookings.mutate(
      {
        showId,
        seats: selectedSeats.map((s) => ({
          rowNumber: s.rowNumber,
          seatNumber: s.seatNumber,
        })),
      },
      {
        onSuccess: (result) => {
          setBulkResult(result);
          // Clear selections that succeeded — leave failures visible
          setSelectedSeats((curr) =>
            curr.filter((seat) =>
              result.failed.some(
                (f) => f.rowNumber === seat.rowNumber && f.seatNumber === seat.seatNumber
              )
            )
          );
        },
      }
    );
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <button
        onClick={() => navigate('/')}
        className="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-900 mb-4 transition-colors"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to shows
      </button>

      {showQ.isLoading && <SkeletonHeader />}
      {showQ.isError && (
        <Alert variant="error">Couldn't load show. {extractErrorMessage(showQ.error)}</Alert>
      )}

      {showQ.data && (
        <Card className="mb-6 overflow-hidden">
          <ShowPoster
            showId={showQ.data.id}
            title={showQ.data.title}
            variant="hero"
          />
          <CardContent className="p-6">
            {showQ.data.description && (
              <p className="text-slate-700 mb-4 leading-relaxed">{showQ.data.description}</p>
            )}
            <div className="flex flex-wrap gap-4 text-sm text-slate-600">
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100">
                <Calendar className="h-3.5 w-3.5" />
                {formatDate(showQ.data.showTime)}
              </span>
              <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100">
                <MapPin className="h-3.5 w-3.5" />
                {showQ.data.venue}
              </span>
            </div>
          </CardContent>
        </Card>
      )}

      <Card className="overflow-hidden">
        <CardContent className="p-6">
          <div className="flex items-baseline justify-between mb-4">
            <h2 className="text-lg font-semibold">Choose your seats</h2>
            {selectedSeats.length > 0 && (
              <span className="text-sm text-slate-500 animate-fade-in">
                {selectedSeats.length} selected
              </span>
            )}
          </div>

          {seatMapQ.isLoading && <SeatMapSkeleton />}
          {seatMapQ.isError && (
            <Alert variant="error">
              Couldn't load seat map. {extractErrorMessage(seatMapQ.error)}
            </Alert>
          )}

          {seatMapQ.data && (
            <SeatPicker
              seatMap={seatMapQ.data}
              selectedSeats={selectedSeats}
              onSelect={toggleSeat}
            />
          )}

          {/* Bulk booking result */}
          {bulkResult && bulkResult.succeeded.length > 0 && bulkResult.failed.length === 0 && (
            <Alert variant="success" className="mt-6">
              {bulkResult.succeeded.length === 1
                ? 'Seat booked! '
                : `All ${bulkResult.succeeded.length} seats booked! `}
              View in{' '}
              <button
                onClick={() => navigate('/bookings')}
                className="font-medium underline underline-offset-2"
              >
                My Bookings
              </button>
              .
            </Alert>
          )}

          {bulkResult && bulkResult.succeeded.length > 0 && bulkResult.failed.length > 0 && (
            <Alert variant="warning" className="mt-6">
              <strong>{bulkResult.succeeded.length}</strong> seat
              {bulkResult.succeeded.length === 1 ? '' : 's'} booked,{' '}
              <strong>{bulkResult.failed.length}</strong> failed.{' '}
              {bulkResult.failed.map((f, i) => (
                <span key={i} className="block text-sm mt-1">
                  Row {f.rowNumber}, Seat {f.seatNumber}: {f.reason}
                </span>
              ))}
            </Alert>
          )}

          {bulkResult && bulkResult.succeeded.length === 0 && bulkResult.failed.length > 0 && (
            <Alert variant="error" className="mt-6">
              Booking failed. {bulkResult.failed[0]?.reason}
            </Alert>
          )}

          {createBookings.isError && (
            <Alert variant="error" className="mt-6">
              {extractErrorMessage(createBookings.error)}
            </Alert>
          )}
        </CardContent>

        {/* Sticky action bar — appears when seats are selected */}
        {selectedSeats.length > 0 && (
          <div className="border-t border-slate-200 px-4 sm:px-6 py-4 bg-gradient-to-r from-slate-50 to-white animate-fade-in-up">
            <div className="flex items-center justify-between gap-4 flex-wrap">
              <div className="flex-1 min-w-0">
                <div className="text-xs text-slate-500 uppercase tracking-wide">
                  {selectedSeats.length} seat{selectedSeats.length === 1 ? '' : 's'}
                </div>
                {/* Compact seat list */}
                <div className="mt-1 flex flex-wrap gap-1.5">
                  {selectedSeats.map((s) => (
                    <button
                      key={`${s.rowNumber}-${s.seatNumber}`}
                      onClick={() => toggleSeat(s)}
                      className="inline-flex items-center gap-1 px-2 py-1 rounded-md bg-indigo-50 border border-indigo-200 text-indigo-700 text-xs font-medium hover:bg-indigo-100 transition-colors"
                      title="Click to deselect"
                    >
                      R{s.rowNumber}·S{s.seatNumber}
                      <X className="h-3 w-3" />
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex items-center gap-3 shrink-0">
                <div className="text-right">
                  <div className="text-xs text-slate-500">Total</div>
                  <div className="text-xl font-bold text-slate-900">
                    {formatPrice(totalPrice)}
                  </div>
                </div>
                <Button
                  size="lg"
                  onClick={handleBook}
                  loading={createBookings.isPending}
                  className="shadow-md"
                >
                  {isLoggedIn
                    ? `Book ${selectedSeats.length} seat${selectedSeats.length === 1 ? '' : 's'}`
                    : 'Log in to book'}
                </Button>
              </div>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
}

function SkeletonHeader() {
  return (
    <Card className="mb-6 overflow-hidden">
      <div className="h-56 sm:h-72 bg-gradient-to-br from-slate-100 to-slate-200 animate-pulse" />
      <CardContent className="p-6 animate-pulse">
        <div className="h-4 bg-slate-100 rounded w-3/4 mb-3" />
        <div className="h-4 bg-slate-100 rounded w-1/2" />
      </CardContent>
    </Card>
  );
}

function SeatMapSkeleton() {
  return (
    <div className="space-y-4 animate-pulse">
      <div className="h-6 bg-slate-100 rounded w-2/3 mx-auto" />
      {[12, 16, 20].map((n, i) => (
        <div key={i} className="flex justify-center gap-1.5">
          {Array.from({ length: n }).map((_, j) => (
            <div key={j} className="h-8 w-8 bg-slate-200 rounded-md" />
          ))}
        </div>
      ))}
    </div>
  );
}
