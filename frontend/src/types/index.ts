// Mirrors backend DTOs. Kept in one place so the rest of the app
// has a single source of truth for shapes.

export type Role = 'USER' | 'ADMIN';

export interface User {
  id: number;
  nic: string;
  name: string;
  surname: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInMs: number;
  user: User;
}

export interface Show {
  id: number;
  title: string;
  description: string | null;
  venue: string;
  showTime: string;
  availableSeats: number;
  totalSeats: number;
}

export type SeatStatus = 'AVAILABLE' | 'BOOKED';

export interface Seat {
  id: number;
  rowNumber: number;
  seatNumber: number;
  price: string;
  status: SeatStatus;
}

export interface SeatMap {
  showId: number;
  showTitle: string;
  rows: {
    rowNumber: number;
    totalSeats: number;
    seats: Seat[];
  }[];
}

export type BookingStatus = 'CONFIRMED' | 'CANCELLED';

export interface Booking {
  id: number;
  userId: number;
  showId: number;
  rowNumber: number;
  seatNumber: number;
  price: string;
  status: BookingStatus;
  createdAt: string;
}

export interface BookingSummary {
  totalBookings: number;
  totalPrice: string;
}

export interface ApiError {
  title: string;
  detail: string;
  status: number;
  errors?: Record<string, string>;
}
