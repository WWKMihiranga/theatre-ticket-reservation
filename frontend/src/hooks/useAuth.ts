import { useMutation } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { AuthResponse } from '@/types';

interface RegisterPayload {
  nic: string;
  name: string;
  surname: string;
  email: string;
  password: string;
}

interface LoginPayload {
  email: string;
  password: string;
}

export function useRegister() {
  return useMutation({
    mutationFn: async (data: RegisterPayload) => {
      const res = await api.post<AuthResponse>('/api/v1/auth/register', data);
      return res.data;
    },
  });
}

export function useLogin() {
  return useMutation({
    mutationFn: async (data: LoginPayload) => {
      const res = await api.post<AuthResponse>('/api/v1/auth/login', data);
      return res.data;
    },
  });
}
