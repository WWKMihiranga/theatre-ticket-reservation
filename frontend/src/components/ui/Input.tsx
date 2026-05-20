import { forwardRef, type InputHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, error, ...props }, ref) => {
    return (
      <input
        ref={ref}
        className={cn(
          'h-10 w-full rounded-md border bg-white px-3 text-sm',
          'placeholder:text-slate-400',
          'focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500',
          'disabled:cursor-not-allowed disabled:opacity-50',
          error ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : 'border-slate-300',
          className
        )}
        {...props}
      />
    );
  }
);
Input.displayName = 'Input';
