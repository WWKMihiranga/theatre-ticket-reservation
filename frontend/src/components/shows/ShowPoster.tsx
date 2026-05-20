import { Drama } from 'lucide-react';
import { getShowTheme, getShowPatternSVG } from '@/lib/posters';
import { cn } from '@/lib/utils';

interface ShowPosterProps {
  showId: number;
  title?: string;
  /** Smaller, for cards in the listing */
  variant?: 'card' | 'hero';
  className?: string;
}

/**
 * Visual poster for a show. Uses a deterministic gradient + decorative SVG
 * pattern based on the show ID — same show always looks the same, different
 * shows are visually distinct.
 */
export function ShowPoster({ showId, title, variant = 'card', className }: ShowPosterProps) {
  const theme = getShowTheme(showId);
  const pattern = getShowPatternSVG(theme.pattern);

  return (
    <div
      className={cn(
        'relative overflow-hidden',
        variant === 'card' ? 'h-40' : 'h-56 sm:h-72',
        className
      )}
      style={{ background: theme.gradient }}
      // SVG patterns are static; safe to inject as innerHTML
      aria-hidden
    >
      {/* Decorative SVG overlay */}
      <div
        className="absolute inset-0"
        dangerouslySetInnerHTML={{ __html: pattern }}
      />

      {/* Subtle radial vignette so text always reads */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-transparent" />

      {/* Drama mask icon, big and faded — adds depth */}
      <Drama
        className={cn(
          'absolute text-white/15',
          variant === 'card'
            ? 'h-32 w-32 -right-4 -bottom-4'
            : 'h-56 w-56 -right-8 -bottom-8'
        )}
        strokeWidth={1.2}
        aria-hidden
      />

      {/* Optional title overlay for the hero variant */}
      {title && variant === 'hero' && (
        <div className="absolute inset-0 flex items-end p-6 sm:p-8">
          <h1
            className="text-3xl sm:text-4xl font-bold tracking-tight text-white drop-shadow-lg leading-tight max-w-2xl"
            style={{ textShadow: '0 2px 12px rgba(0,0,0,0.4)' }}
          >
            {title}
          </h1>
        </div>
      )}
    </div>
  );
}
