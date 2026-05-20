import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { FormField } from '@/components/ui/FormField';
import { Alert } from '@/components/ui/Alert';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/Card';
import { useCreateShow } from '@/hooks/useShows';
import { extractErrorMessage } from '@/lib/api';

const schema = z.object({
  title: z.string().min(1, 'Title is required').max(200),
  description: z.string().max(2000).optional(),
  venue: z.string().min(1, 'Venue is required').max(200),
  showTime: z.string().min(1, 'Show time is required'),
});

type FormData = z.infer<typeof schema>;

export function CreateShowPage() {
  const navigate = useNavigate();
  const create = useCreateShow();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { venue: 'New Theatre' },
  });

  const onSubmit = (data: FormData) => {
    // Convert datetime-local value to ISO so Spring's @Future passes
    const payload = {
      ...data,
      description: data.description ?? '',
      showTime: new Date(data.showTime).toISOString(),
    };
    create.mutate(payload, {
      onSuccess: (show) => navigate(`/shows/${show.id}`),
    });
  };

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
      <Card>
        <CardHeader>
          <CardTitle>Create a new show</CardTitle>
          <CardDescription>
            48 seats (12 + 16 + 20) at $10 / $20 / $30 will be generated automatically.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <FormField label="Title" htmlFor="title" error={errors.title?.message}>
              <Input
                id="title"
                placeholder="Hamlet — Opening Night"
                error={!!errors.title}
                {...register('title')}
              />
            </FormField>

            <FormField
              label="Description"
              htmlFor="description"
              error={errors.description?.message}
              hint="Optional"
            >
              <textarea
                id="description"
                rows={3}
                className="w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm placeholder:text-slate-400 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                placeholder="A timeless tragedy by William Shakespeare."
                {...register('description')}
              />
            </FormField>

            <FormField label="Venue" htmlFor="venue" error={errors.venue?.message}>
              <Input
                id="venue"
                placeholder="New Theatre"
                error={!!errors.venue}
                {...register('venue')}
              />
            </FormField>

            <FormField
              label="Show date and time"
              htmlFor="showTime"
              error={errors.showTime?.message}
              hint="Must be in the future"
            >
              <Input
                id="showTime"
                type="datetime-local"
                error={!!errors.showTime}
                {...register('showTime')}
              />
            </FormField>

            {create.isError && (
              <Alert variant="error">{extractErrorMessage(create.error)}</Alert>
            )}

            <div className="flex justify-end gap-2 pt-2">
              <Button type="button" variant="secondary" onClick={() => navigate('/')}>
                Cancel
              </Button>
              <Button type="submit" loading={create.isPending}>
                Create show
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
