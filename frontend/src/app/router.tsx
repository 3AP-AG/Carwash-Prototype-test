// All routes. Each feature exports its routes from features/<feature>/routes.tsx; add them here.
// The entry pages (/gate, /login) are not routes: Spring serves them outside this bundle.
import { createBrowserRouter } from 'react-router';
import { bookingRoutes } from '@/features/bookings/routes';
import { exampleRoutes } from '@/features/examples/routes';
import { PageLayout } from '@/ui';

export const router = createBrowserRouter([
  {
    element: <PageLayout />,
    children: [...bookingRoutes, ...exampleRoutes],
  },
]);
