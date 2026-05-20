import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import type { User } from '@/types';

interface AuthState {
  token: string | null;
  user: User | null;
  setAuth: (token: string, user: User) => void;
  clearAuth: () => void;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      setAuth: (token, user) => set({ token, user }),
      clearAuth: () => set({ token: null, user: null }),
      isAdmin: () => get().user?.role === 'ADMIN',
    }),
    {
      name: 'theatre-auth',
      storage: createJSONStorage(() => localStorage),
      // Only persist the actual state, not the methods.
      partialize: (state) => ({ token: state.token, user: state.user }),
    }
  )
);
