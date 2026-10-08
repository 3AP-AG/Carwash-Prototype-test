// Routes of the bookings feature. Registered in app/router.tsx.
import type { RouteObject } from 'react-router';
import { BookingConfirmationPage } from './BookingConfirmationPage';
import { BookingFormPage } from './BookingFormPage';

export const bookingRoutes: RouteObject[] = [
  { index: true, element: <BookingFormPage /> },
  { path: 'bookings/:id', element: <BookingConfirmationPage /> },
];
