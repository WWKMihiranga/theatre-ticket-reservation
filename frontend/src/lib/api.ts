import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/store/auth';
import type { ApiError } from '@/types';

const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Endpoints that are always public — even if we have a JWT in the store,
 * we don't attach it on these paths. This means:
 *   1. A stale/invalid JWT can't break public browsing.
 *   2. Public endpoints don't see auth context, matching backend expectations.
 */
function isPublicPath(url: string | undefined, method: string | undefined): boolean {
  if (!url) return false;
  const m = (method ?? 'GET').toUpperCase();
  if (url.startsWith('/api/v1/auth/')) return true;
  if (m === 'GET' && url.startsWith('/api/v1/shows')) return true;
  return false;
}

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().token;
  if (token && !isPublicPath(config.url, config.method)) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Stale or invalid token. Clear it so the next render reflects logged-out state.
      // Skip on the login/register pages to avoid clobbering an in-flight login attempt.
      const path = window.location.pathname;
      if (path !== '/login' && path !== '/register') {
        useAuthStore.getState().clearAuth();
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Extracts a user-friendly message from an axios error.
 * Backend uses RFC 7807 problem details which arrive in error.response.data.
 */
export function extractErrorMessage(err: unknown): string {
  if (axios.isAxiosError(err)) {
    const data = err.response?.data as ApiError | undefined;
    if (data?.errors) {
      return Object.entries(data.errors)
        .map(([f, m]) => `${f}: ${m}`)
        .join('; ');
    }
    if (data?.detail) return data.detail;
    if (err.code === 'ERR_NETWORK') {
      return 'Network error — is the API gateway running on port 8080?';
    }
    if (err.message) return err.message;
  }
  return 'Something went wrong';
}
