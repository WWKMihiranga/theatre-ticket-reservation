import { Link } from 'react-router-dom';
import { Calendar, MapPin, ArrowRight, Sparkles, Ticket } from 'lucide-react';
import { useShows } from '@/hooks/useShows';
import { Card, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Alert } from '@/components/ui/Alert';
import { ShowPoster } from '@/components/shows/ShowPoster';
import { formatDate } from '@/lib/utils';
import type { Show } from '@/types';

export function ShowsListPage() {
  const { data: shows, isLoading, isError, error } = useShows();

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <Hero />

      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-xl font-semibold tracking-tight text-slate-900">
          Upcoming shows
        </h2>
        {shows && shows.length > 0 && (
          <span className="text-sm text-slate-500">
            {shows.length} {shows.length === 1 ? 'show' : 'shows'} available
          </span>
        )}
      </div>

      {isLoading && <ShowsSkeleton />}

      {isError && (
        <Alert variant="error">
          Couldn't load shows. {error instanceof Error ? error.message : ''}
        </Alert>
      )}

      {shows && shows.length === 0 && (
        <Card>
          <CardContent className="py-16 text-center">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 mb-3">
              <Ticket className="h-5 w-5 text-slate-400" />
            </div>
            <p className="text-slate-600 font-medium">No shows scheduled yet</p>
            <p className="text-sm text-slate-500 mt-1">Check back soon — new shows added weekly.</p>
          </CardContent>
        </Card>
      )}

      {shows && shows.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {shows.map((show, i) => (
            <ShowCard key={show.id} show={show} animationDelay={i * 80} />
          ))}
        </div>
      )}
    </div>
  );
}

function Hero() {
  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-600 p-8 sm:p-12 mb-10 shadow-lg">
      {/* Decorative blurred circles */}
      <div className="absolute -top-10 -right-10 h-40 w-40 rounded-full bg-white/10 blur-2xl" />
      <div className="absolute -bottom-10 -left-10 h-32 w-32 rounded-full bg-white/10 blur-2xl" />

      <div className="relative">
        <div className="inline-flex items-center gap-2 rounded-full bg-white/15 backdrop-blur-sm px-3 py-1 text-xs font-medium text-white mb-4 animate-fade-in">
          <Sparkles className="h-3.5 w-3.5" />
          Booking now open
        </div>
        <h1 className="text-3xl sm:text-5xl font-bold text-white tracking-tight leading-tight max-w-2xl">
          Live theatre,<br/>
          <span className="bg-gradient-to-r from-yellow-200 to-orange-200 bg-clip-text text-transparent">
            unforgettable nights.
          </span>
        </h1>
        <p className="mt-3 text-white/85 max-w-lg text-sm sm:text-base">
          Discover upcoming performances at the New Theatre. Pick your seat, book in seconds, no fuss.
        </p>
      </div>
    </div>
  );
}

function ShowCard({ show, animationDelay }: { show: Show; animationDelay: number }) {
  const soldOut = show.availableSeats === 0;
  const almostGone = show.availableSeats > 0 && show.availableSeats <= 5;

  return (
    <Link
      to={`/shows/${show.id}`}
      className="group block focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500 focus-visible:ring-offset-2 rounded-xl animate-fade-in-up"
      style={{ animationDelay: `${animationDelay}ms` }}
    >
      <Card className="h-full overflow-hidden border-slate-200 transition-all duration-300 group-hover:-translate-y-1 group-hover:shadow-xl group-hover:border-indigo-200">
        {/* Poster */}
        <div className="relative">
          <ShowPoster showId={show.id} variant="card" />
          <div className="absolute top-3 right-3">
            {soldOut ? (
              <Badge variant="danger" className="backdrop-blur bg-red-500/90 text-white border-red-400">
                Sold out
              </Badge>
            ) : almostGone ? (
              <Badge variant="warning" className="backdrop-blur bg-amber-500/90 text-white border-amber-400">
                {show.availableSeats} left
              </Badge>
            ) : (
              <Badge variant="success" className="backdrop-blur bg-emerald-500/90 text-white border-emerald-400">
                {show.availableSeats} seats
              </Badge>
            )}
          </div>
        </div>

        <CardContent className="p-5">
          <h3 className="text-lg font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-2 mb-1">
            {show.title}
          </h3>
          {show.description && (
            <p className="text-sm text-slate-600 mb-4 line-clamp-2">{show.description}</p>
          )}

          <div className="space-y-1.5 text-sm text-slate-500">
            <div className="flex items-center gap-2">
              <Calendar className="h-3.5 w-3.5" />
              {formatDate(show.showTime)}
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="h-3.5 w-3.5" />
              {show.venue}
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-slate-100 flex items-center justify-between text-sm">
            <span className="text-slate-500">
              <span className="text-slate-400">from</span>{' '}
              <span className="font-semibold text-slate-900">$10</span>
            </span>
            <span className="inline-flex items-center gap-1 text-indigo-600 font-medium group-hover:gap-2 transition-all">
              Book now
              <ArrowRight className="h-4 w-4" />
            </span>
          </div>
        </CardContent>
      </Card>
    </Link>
  );
}

function ShowsSkeleton() {
  return (
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: 3 }).map((_, i) => (
        <Card key={i} className="overflow-hidden">
          <div className="h-40 bg-slate-100 animate-pulse" />
          <CardContent className="p-5 animate-pulse">
            <div className="h-5 bg-slate-200 rounded w-3/4 mb-3" />
            <div className="h-4 bg-slate-100 rounded w-full mb-2" />
            <div className="h-4 bg-slate-100 rounded w-1/2 mb-4" />
            <div className="h-4 bg-slate-100 rounded w-1/3" />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
