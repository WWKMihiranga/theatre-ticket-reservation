import { cn } from '@/lib/utils';
import type { Seat, SeatMap } from '@/types';

interface SeatPickerProps {
  seatMap: SeatMap;
  selectedSeats: Seat[];
  onSelect: (seat: Seat) => void;
}

/**
 * Renders the coursework's three-row theatre layout as a clickable grid.
 *
 *   Row 1: 12 seats @ $10  (front)
 *   Row 2: 16 seats @ $20
 *   Row 3: 20 seats @ $30  (back)
 *
 * Multiple seats can be selected; clicking a selected seat deselects it.
 *
 * Colors encode status:
 *   green  = available
 *   indigo = selected
 *   gray   = booked (not clickable)
 */
export function SeatPicker({ seatMap, selectedSeats, onSelect }: SeatPickerProps) {
  const isSelected = (seat: Seat) =>
    selectedSeats.some(
      (s) => s.rowNumber === seat.rowNumber && s.seatNumber === seat.seatNumber
    );

  return (
    <div className="space-y-6">
      {/* Stage with a soft glow underneath */}
      <div className="relative">
        <div className="mx-auto h-7 w-3/4 bg-gradient-to-b from-slate-900 to-slate-700 rounded-t-full shadow-lg" />
        <div className="mx-auto h-2 w-1/2 bg-gradient-to-b from-amber-200/40 to-transparent blur-sm -mt-1" />
        <p className="text-center text-xs uppercase tracking-[0.2em] text-slate-500 mt-2 font-medium">
          Stage
        </p>
      </div>

      {/* Rows */}
      <div className="space-y-3">
        {seatMap.rows.map((row) => (
          <div key={row.rowNumber} className="flex items-center gap-3">
            <div className="w-16 shrink-0 text-xs text-slate-500 text-right">
              <div className="font-medium text-slate-700">Row {row.rowNumber}</div>
              <div className="text-[10px] text-slate-400">${getPriceForRow(row.rowNumber)}</div>
            </div>
            <div
              className="flex-1 flex flex-wrap gap-1.5 justify-center"
              role="group"
              aria-label={`Row ${row.rowNumber} seats`}
            >
              {row.seats.map((seat) => (
                <SeatButton
                  key={seat.id}
                  seat={seat}
                  isSelected={isSelected(seat)}
                  onClick={() => seat.status === 'AVAILABLE' && onSelect(seat)}
                />
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Legend */}
      <div className="flex items-center justify-center gap-6 pt-2 text-xs text-slate-600">
        <LegendItem className="bg-emerald-100 border-emerald-300" label="Available" />
        <LegendItem className="bg-indigo-600 border-indigo-700" label="Selected" />
        <LegendItem className="bg-slate-200 border-slate-300" label="Booked" />
      </div>
    </div>
  );
}

function SeatButton({
  seat,
  isSelected,
  onClick,
}: {
  seat: Seat;
  isSelected: boolean;
  onClick: () => void;
}) {
  const isBooked = seat.status === 'BOOKED';

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={isBooked}
      title={`Row ${seat.rowNumber}, Seat ${seat.seatNumber} — $${seat.price} (${seat.status})`}
      aria-label={`Row ${seat.rowNumber}, Seat ${seat.seatNumber}, ${seat.status}`}
      aria-pressed={isSelected}
      className={cn(
        'h-8 w-8 rounded-md border text-[10px] font-semibold transition-all duration-200',
        'flex items-center justify-center',
        isBooked && 'bg-slate-200 border-slate-300 text-slate-400 cursor-not-allowed line-through opacity-60',
        !isBooked && !isSelected &&
          'bg-emerald-100 border-emerald-300 text-emerald-900 hover:bg-emerald-200 hover:border-emerald-400 hover:-translate-y-0.5 hover:shadow-sm cursor-pointer',
        isSelected &&
          'bg-indigo-600 border-indigo-700 text-white scale-110 shadow-md animate-pop'
      )}
    >
      {seat.seatNumber}
    </button>
  );
}

function LegendItem({ className, label }: { className: string; label: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <span className={cn('h-3 w-3 rounded border', className)} />
      <span>{label}</span>
    </div>
  );
}

function getPriceForRow(row: number): string {
  switch (row) {
    case 1:
      return '10';
    case 2:
      return '20';
    case 3:
      return '30';
    default:
      return '?';
  }
}
