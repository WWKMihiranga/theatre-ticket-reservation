import { Link, NavLink, useNavigate } from 'react-router-dom';
import { Ticket, LogOut } from 'lucide-react';
import { useAuthStore } from '@/store/auth';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/Button';

export function Navbar() {
  const { user, clearAuth, isAdmin } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    clearAuth();
    navigate('/');
  };

  const linkClass = ({ isActive }: { isActive: boolean }) =>
    cn(
      'px-3 py-2 rounded-md text-sm font-medium transition-colors',
      isActive
        ? 'bg-slate-100 text-slate-900'
        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'
    );

  return (
    <header className="bg-white border-b border-slate-200 sticky top-0 z-30">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center gap-2 group">
              <Ticket className="h-5 w-5 text-indigo-600 group-hover:rotate-12 transition-transform" />
              <span className="font-semibold tracking-tight text-slate-900">Theatre</span>
            </Link>
            <nav className="hidden sm:flex items-center gap-1">
              <NavLink to="/" end className={linkClass}>
                Shows
              </NavLink>
              {user && (
                <NavLink to="/bookings" className={linkClass}>
                  My Bookings
                </NavLink>
              )}
              {isAdmin() && (
                <NavLink to="/admin/shows/new" className={linkClass}>
                  Create Show
                </NavLink>
              )}
            </nav>
          </div>

          <div className="flex items-center gap-3">
            {user ? (
              <>
                <span className="hidden sm:inline text-sm text-slate-600">
                  {user.name} {user.surname}
                </span>
                <Button variant="ghost" size="sm" onClick={handleLogout}>
                  <LogOut className="h-3.5 w-3.5" />
                  <span className="hidden sm:inline">Log out</span>
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="ghost" size="sm">
                    Log in
                  </Button>
                </Link>
                <Link to="/register">
                  <Button size="sm">Sign up</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
