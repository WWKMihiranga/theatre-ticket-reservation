import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/Button';

export function NotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center px-4 py-24 text-center">
      <div className="text-6xl font-semibold text-slate-300 mb-4">404</div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-2">Page not found</h1>
      <p className="text-slate-500 mb-6">The page you're looking for doesn't exist.</p>
      <Link to="/">
        <Button>Go home</Button>
      </Link>
    </div>
  );
}
