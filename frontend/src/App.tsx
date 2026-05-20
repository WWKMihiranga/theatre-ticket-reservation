import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ShowsListPage } from '@/pages/ShowsListPage';
import { ShowDetailPage } from '@/pages/ShowDetailPage';
import { LoginPage } from '@/pages/LoginPage';
import { RegisterPage } from '@/pages/RegisterPage';
import { MyBookingsPage } from '@/pages/MyBookingsPage';
import { CreateShowPage } from '@/pages/CreateShowPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { ProtectedRoute, AdminRoute } from '@/routes/ProtectedRoute';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<ShowsListPage />} />
          <Route path="shows/:id" element={<ShowDetailPage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route
            path="bookings"
            element={
              <ProtectedRoute>
                <MyBookingsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="admin/shows/new"
            element={
              <AdminRoute>
                <CreateShowPage />
              </AdminRoute>
            }
          />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
