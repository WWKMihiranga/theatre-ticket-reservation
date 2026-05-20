import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { Show, SeatMap } from '@/types';

export function useShows() {
  return useQuery({
    queryKey: ['shows'],
    queryFn: async () => {
      const res = await api.get<Show[]>('/api/v1/shows');
      return res.data;
    },
  });
}

export function useShow(id: number | undefined) {
  return useQuery({
    queryKey: ['shows', id],
    queryFn: async () => {
      const res = await api.get<Show>(`/api/v1/shows/${id}`);
      return res.data;
    },
    enabled: id != null,
  });
}

export function useSeatMap(showId: number | undefined) {
  return useQuery({
    queryKey: ['shows', showId, 'seats'],
    queryFn: async () => {
      const res = await api.get<SeatMap>(`/api/v1/shows/${showId}/seats`);
      return res.data;
    },
    enabled: showId != null,
  });
}

interface CreateShowPayload {
  title: string;
  description: string;
  venue: string;
  showTime: string;
}

export function useCreateShow() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (data: CreateShowPayload) => {
      const res = await api.post<Show>('/api/v1/shows', data);
      return res.data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['shows'] });
    },
  });
}
