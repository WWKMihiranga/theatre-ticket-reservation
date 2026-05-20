import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Ticket } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { FormField } from '@/components/ui/FormField';
import { Alert } from '@/components/ui/Alert';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { useLogin } from '@/hooks/useAuth';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/lib/api';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});

type FormData = z.infer<typeof schema>;

export function LoginPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const login = useLogin();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = (data: FormData) => {
    login.mutate(data, {
      onSuccess: (res) => {
        setAuth(res.accessToken, res.user);
        navigate('/');
      },
    });
  };

  return (
    <div className="flex items-center justify-center px-4 py-12">
      <Card className="w-full max-w-md animate-fade-in-up shadow-lg">
        <CardHeader>
          <div className="flex justify-center mb-2">
            <div className="relative">
              <div className="absolute inset-0 bg-indigo-300 rounded-full blur-xl opacity-40 animate-pulse-soft" />
              <Ticket className="relative h-9 w-9 text-indigo-600" />
            </div>
          </div>
          <CardTitle className="text-center">Welcome back</CardTitle>
          <CardDescription className="text-center">
            Log in to book your seat
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Email" htmlFor="email" error={errors.email?.message}>
              <Input
                id="email"
                type="email"
                placeholder="you@example.com"
                autoComplete="email"
                error={!!errors.email}
                {...register('email')}
              />
            </FormField>

            <FormField label="Password" htmlFor="password" error={errors.password?.message}>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                autoComplete="current-password"
                error={!!errors.password}
                {...register('password')}
              />
            </FormField>

            {login.isError && (
              <Alert variant="error">{extractErrorMessage(login.error)}</Alert>
            )}

            <Button type="submit" className="w-full" loading={login.isPending}>
              Log in
            </Button>

            <p className="text-center text-sm text-slate-600">
              Don't have an account?{' '}
              <Link to="/register" className="text-indigo-600 hover:text-indigo-700 font-medium">
                Sign up
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
