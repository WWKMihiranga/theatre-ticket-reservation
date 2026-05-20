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
import { useRegister } from '@/hooks/useAuth';
import { useAuthStore } from '@/store/auth';
import { extractErrorMessage } from '@/lib/api';

// Validation rules mirror the backend's @Pattern, @Email, and @Size constraints
const schema = z.object({
  nic: z
    .string()
    .regex(/^[A-Za-z0-9]{6,20}$/, 'NIC must be 6-20 alphanumeric characters'),
  name: z.string().min(1, 'Name is required').max(80),
  surname: z.string().min(1, 'Surname is required').max(80),
  email: z.string().email('Enter a valid email').max(160),
  password: z.string().min(8, 'Password must be at least 8 characters').max(100),
});

type FormData = z.infer<typeof schema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const reg = useRegister();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = (data: FormData) => {
    reg.mutate(data, {
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
          <CardTitle className="text-center">Create your account</CardTitle>
          <CardDescription className="text-center">
            Get started with your first booking
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              label="NIC"
              htmlFor="nic"
              error={errors.nic?.message}
              hint="6-20 alphanumeric characters"
            >
              <Input
                id="nic"
                placeholder="200012345V"
                error={!!errors.nic}
                {...register('nic')}
              />
            </FormField>

            <div className="grid grid-cols-2 gap-3">
              <FormField label="First name" htmlFor="name" error={errors.name?.message}>
                <Input id="name" placeholder="Alex" error={!!errors.name} {...register('name')} />
              </FormField>
              <FormField label="Surname" htmlFor="surname" error={errors.surname?.message}>
                <Input
                  id="surname"
                  placeholder="Perera"
                  error={!!errors.surname}
                  {...register('surname')}
                />
              </FormField>
            </div>

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

            <FormField
              label="Password"
              htmlFor="password"
              error={errors.password?.message}
              hint="At least 8 characters"
            >
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                autoComplete="new-password"
                error={!!errors.password}
                {...register('password')}
              />
            </FormField>

            {reg.isError && (
              <Alert variant="error">{extractErrorMessage(reg.error)}</Alert>
            )}

            <Button type="submit" className="w-full" loading={reg.isPending}>
              Create account
            </Button>

            <p className="text-center text-sm text-slate-600">
              Already have an account?{' '}
              <Link to="/login" className="text-indigo-600 hover:text-indigo-700 font-medium">
                Log in
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
