import { AlertCircle, CheckCircle2, Info, XCircle } from 'lucide-react';
import type { ReactNode } from 'react';
import { cn } from '@/lib/utils';

type AlertVariant = 'info' | 'success' | 'warning' | 'error';

const config: Record<AlertVariant, { wrap: string; icon: typeof Info }> = {
  info: { wrap: 'bg-blue-50 border-blue-200 text-blue-900', icon: Info },
  success: { wrap: 'bg-emerald-50 border-emerald-200 text-emerald-900', icon: CheckCircle2 },
  warning: { wrap: 'bg-amber-50 border-amber-200 text-amber-900', icon: AlertCircle },
  error: { wrap: 'bg-red-50 border-red-200 text-red-900', icon: XCircle },
};

export function Alert({
  variant = 'info',
  children,
  className,
}: {
  variant?: AlertVariant;
  children: ReactNode;
  className?: string;
}) {
  const { wrap, icon: Icon } = config[variant];
  return (
    <div className={cn('flex items-start gap-3 rounded-md border p-3 text-sm', wrap, className)} role="alert">
      <Icon className="h-4 w-4 mt-0.5 shrink-0" />
      <div>{children}</div>
    </div>
  );
}
