/**
 * Deterministic visual identity per show — no DB changes needed.
 *
 * Each show gets a unique gradient + decorative SVG pattern derived from its ID.
 * The result is consistent across renders (so the same show always looks the
 * same) and visually distinct between shows.
 *
 * We picked 8 carefully-chosen gradient palettes that all work well with white
 * text overlay and feel theatrical/cinematic.
 */

interface PosterTheme {
  gradient: string;
  accent: string;
  pattern: 'rays' | 'curtain' | 'spotlight' | 'dots' | 'arcs';
}

const THEMES: PosterTheme[] = [
  {
    gradient: 'linear-gradient(135deg, #4338ca 0%, #7c3aed 50%, #db2777 100%)',
    accent: '#fbbf24',
    pattern: 'spotlight',
  },
  {
    gradient: 'linear-gradient(135deg, #0f172a 0%, #1e40af 50%, #0891b2 100%)',
    accent: '#f59e0b',
    pattern: 'rays',
  },
  {
    gradient: 'linear-gradient(135deg, #7f1d1d 0%, #b91c1c 50%, #f59e0b 100%)',
    accent: '#fef3c7',
    pattern: 'curtain',
  },
  {
    gradient: 'linear-gradient(135deg, #064e3b 0%, #047857 50%, #06b6d4 100%)',
    accent: '#fde047',
    pattern: 'arcs',
  },
  {
    gradient: 'linear-gradient(135deg, #1e1b4b 0%, #4c1d95 50%, #c026d3 100%)',
    accent: '#f0abfc',
    pattern: 'dots',
  },
  {
    gradient: 'linear-gradient(135deg, #831843 0%, #be185d 50%, #f97316 100%)',
    accent: '#fef9c3',
    pattern: 'rays',
  },
  {
    gradient: 'linear-gradient(135deg, #0c4a6e 0%, #0e7490 50%, #14b8a6 100%)',
    accent: '#fef08a',
    pattern: 'spotlight',
  },
  {
    gradient: 'linear-gradient(135deg, #422006 0%, #92400e 50%, #f59e0b 100%)',
    accent: '#fde68a',
    pattern: 'curtain',
  },
];

export function getShowTheme(showId: number): PosterTheme {
  // Deterministic — same ID always picks the same theme
  return THEMES[Math.abs(showId) % THEMES.length];
}

/**
 * SVG decorative overlay for the hero. Each pattern evokes the theatre.
 */
export function getShowPatternSVG(pattern: PosterTheme['pattern']): string {
  switch (pattern) {
    case 'rays':
      // Radiating light beams from the top — like a stage spotlight
      return `<svg viewBox="0 0 400 200" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;width:100%;height:100%;opacity:0.18;pointer-events:none">
        <defs><radialGradient id="r" cx="50%" cy="0%" r="80%"><stop offset="0%" stop-color="#fff" stop-opacity="0.7"/><stop offset="100%" stop-color="#fff" stop-opacity="0"/></radialGradient></defs>
        <path d="M 200 0 L 0 200 L 100 200 Z" fill="url(#r)"/>
        <path d="M 200 0 L 80 200 L 180 200 Z" fill="url(#r)"/>
        <path d="M 200 0 L 220 200 L 320 200 Z" fill="url(#r)"/>
        <path d="M 200 0 L 300 200 L 400 200 Z" fill="url(#r)"/>
      </svg>`;
    case 'curtain':
      // Stage curtain folds
      return `<svg viewBox="0 0 400 200" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;width:100%;height:100%;opacity:0.15;pointer-events:none">
        <path d="M 0 0 Q 25 100 0 200 Z" fill="#fff"/>
        <path d="M 50 0 Q 75 100 50 200 L 0 200 Q 25 100 0 0 Z" fill="#fff" opacity="0.6"/>
        <path d="M 400 0 Q 375 100 400 200 Z" fill="#fff"/>
        <path d="M 350 0 Q 325 100 350 200 L 400 200 Q 375 100 400 0 Z" fill="#fff" opacity="0.6"/>
      </svg>`;
    case 'spotlight':
      // Single elliptical spotlight from above
      return `<svg viewBox="0 0 400 200" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;width:100%;height:100%;opacity:0.25;pointer-events:none">
        <defs><radialGradient id="s" cx="50%" cy="20%" r="60%"><stop offset="0%" stop-color="#fff" stop-opacity="0.9"/><stop offset="100%" stop-color="#fff" stop-opacity="0"/></radialGradient></defs>
        <ellipse cx="200" cy="80" rx="180" ry="120" fill="url(#s)"/>
      </svg>`;
    case 'dots':
      // Bokeh dots — soft, festive
      return `<svg viewBox="0 0 400 200" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;width:100%;height:100%;opacity:0.22;pointer-events:none">
        <circle cx="60" cy="40" r="20" fill="#fff"/>
        <circle cx="320" cy="60" r="14" fill="#fff"/>
        <circle cx="180" cy="30" r="8" fill="#fff"/>
        <circle cx="280" cy="150" r="24" fill="#fff" opacity="0.6"/>
        <circle cx="100" cy="160" r="16" fill="#fff" opacity="0.7"/>
        <circle cx="380" cy="120" r="10" fill="#fff"/>
        <circle cx="220" cy="100" r="6" fill="#fff"/>
      </svg>`;
    case 'arcs':
      // Architectural arches
      return `<svg viewBox="0 0 400 200" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" style="position:absolute;inset:0;width:100%;height:100%;opacity:0.18;pointer-events:none">
        <path d="M 0 200 Q 100 80 200 200 Q 300 80 400 200" stroke="#fff" stroke-width="2" fill="none"/>
        <path d="M 0 200 Q 100 130 200 200 Q 300 130 400 200" stroke="#fff" stroke-width="2" fill="none"/>
        <path d="M 0 200 Q 100 30 200 200 Q 300 30 400 200" stroke="#fff" stroke-width="1.5" fill="none"/>
      </svg>`;
  }
}
