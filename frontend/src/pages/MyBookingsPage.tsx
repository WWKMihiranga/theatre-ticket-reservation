import { useState } from 'react';
import { Trash2, ArrowUpDown, Ticket as TicketIcon, Calendar, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useMyBookings, useBookingSummary, useCancelBooking } from '@/hooks/useBookings';
import { Card, CardContent } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { formatDate, formatPrice } from '@/lib/utils';
import { extractErrorMessage } from '@/lib/api';
import { getShowTheme } from '@/lib/posters';
import type { Booking } from '@/types';

export function MyBookingsPage() {
  const [sortByPrice, setSortByPrice] = useState(false);
  const bookings = useMyBookings(sortByPrice ? 'price' : undefined);
  const summary = useBookingSummary();
  const cancel = useCancelBooking();

  const handleCancel = (id: number) => {
    if (!confirm('Cancel this booking? The seat will be released.')) return;
    cancel.mutate(id);
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      <div className="mb-6">
        <h1 className="text-3xl font-semibold tracking-tight text-slate-900">My Bookings</h1>
        <p className="text-slate-500 mt-1">Manage your reserved seats</p>
      </div>

      {/* Summary card */}
      {summary.data && (
        <Card className="mb-6 overflow-hidden border-indigo-100 animate-fade-in-up">
          <div className="relative bg-gradient-to-br from-indigo-600 via-purple-600 to-indigo-700 p-5 sm:p-6">
            {/* Decorative shapes */}
            <div className="absolute -top-8 -right-8 h-32 w-32 rounded-full bg-white/10 blur-2xl" />
            <div className="absolute -bottom-8 -left-8 h-24 w-24 rounded-full bg-white/10 blur-xl" />

            <div className="relative flex items-center justify-between flex-wrap gap-4">
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-white/20 backdrop-blur flex items-center justify-center">
                  <TicketIcon className="h-6 w-6 text-white" />
                </div>
                <div>
                  <div className="text-sm text-white/80">Total bookings</div>
                  <div className="text-3xl font-bold text-white">
                    {summary.data.totalBookings}
                  </div>
                </div>
              </div>
              <div className="text-right">
                <div className="text-sm text-white/80">Total amount</div>
                <div className="text-3xl font-bold text-white">
                  {formatPrice(summary.data.totalPrice)}
                </div>
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* Sort toggle */}
      {bookings.data && bookings.data.length > 0 && (
        <div className="flex justify-end mb-4">
          <Button
            variant={sortByPrice ? 'primary' : 'secondary'}
            size="sm"
            onClick={() => setSortByPrice((s) => !s)}
          >
            <ArrowUpDown className="h-3.5 w-3.5" />
            {sortByPrice ? 'Sorted by price' : 'Sort by price'}
          </Button>
        </div>
      )}

      {/* Bookings */}
      {bookings.isLoading && <BookingsSkeleton />}

      {bookings.isError && <Alert variant="error">Couldn't load your bookings.</Alert>}

      {bookings.data && bookings.data.length === 0 && <EmptyState />}

      {bookings.data && bookings.data.length > 0 && (
        <div className="space-y-3">
          {bookings.data.map((b, i) => (
            <BookingRow
              key={b.id}
              booking={b}
              onCancel={() => handleCancel(b.id)}
              animationDelay={i * 60}
            />
          ))}
        </div>
      )}

      {cancel.isError && (
        <Alert variant="error" className="mt-4">
          {extractErrorMessage(cancel.error)}
        </Alert>
      )}
    </div>
  );
}

function BookingRow({
  booking,
  onCancel,
  animationDelay,
}: {
  booking: Booking;
  onCancel: () => void;
  animationDelay: number;
}) {
  const theme = getShowTheme(booking.showId);

  return (
    <Card
      className="overflow-hidden transition-all hover:shadow-md hover:border-slate-300 animate-fade-in-up"
      style={{ animationDelay: `${animationDelay}ms` }}
    >
      <div className="flex items-stretch">
        {/* Color accent stripe — matches the show's poster theme */}
        <div className="w-2 shrink-0" style={{ background: theme.gradient }} />

        <div className="flex-1 p-4 sm:p-5 flex items-center justify-between gap-4">
          <div className="flex items-center gap-4 min-w-0">
            <div
              className="h-12 w-12 rounded-lg flex items-center justify-center shrink-0 shadow-sm"
              style={{ background: theme.gradient }}
            >
              <TicketIcon className="h-5 w-5 text-white" />
            </div>
            <div className="min-w-0">
              <Link
                to={`/shows/${booking.showId}`}
                className="font-semibold text-slate-900 hover:text-indigo-600 transition-colors"
              >
                Show #{booking.showId}
              </Link>
              <div className="text-sm text-slate-600 mt-0.5">
                Row {booking.rowNumber}, Seat {booking.seatNumber}
              </div>
              <div className="text-xs text-slate-400 mt-0.5 inline-flex items-center gap-1">
                <Calendar className="h-3 w-3" />
                Booked {formatDate(booking.createdAt)}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <div className="text-right">
              <div className="font-bold text-lg text-slate-900">
                {formatPrice(booking.price)}
              </div>
              <Badge variant="success">{booking.status}</Badge>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={onCancel}
              aria-label="Cancel booking"
              className="hover:bg-red-50 hover:text-red-600"
            >
              <Trash2 className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );
}

function EmptyState() {
  return (
    <Card className="overflow-hidden">
      <CardContent className="py-16 text-center">
        <div className="relative inline-block mb-4">
          <div className="absolute inset-0 bg-indigo-200 rounded-full blur-2xl opacity-50 animate-pulse-soft" />
          <div className="relative inline-flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg">
            <TicketIcon className="h-7 w-7 text-white" />
          </div>
        </div>
        <h3 className="text-lg font-semibold text-slate-900 mb-1">No bookings yet</h3>
        <p className="text-sm text-slate-500 mb-6 max-w-sm mx-auto">
          Your seat reservations will show up here. Pick a show to get started.
        </p>
        <Link to="/">
          <Button>
            <Sparkles className="h-4 w-4" />
            Browse shows
          </Button>
        </Link>
      </CardContent>
    </Card>
  );
}

function BookingsSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 3 }).map((_, i) => (
        <Card key={i} className="overflow-hidden">
          <div className="flex">
            <div className="w-2 bg-slate-200 animate-pulse" />
            <div className="flex-1 p-5 animate-pulse flex justify-between">
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 bg-slate-200 rounded-lg" />
                <div className="space-y-2">
                  <div className="h-4 bg-slate-200 rounded w-40" />
                  <div className="h-3 bg-slate-100 rounded w-32" />
                </div>
              </div>
              <div className="h-6 bg-slate-200 rounded w-20" />
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}
