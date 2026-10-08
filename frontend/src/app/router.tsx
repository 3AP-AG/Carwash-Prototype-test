// All routes. Each feature exports its routes from features/<feature>/routes.tsx; add them here.
// The entry pages (/gate, /login) are not routes: Spring serves them outside this bundle.
import { createBrowserRouter } from 'react-router';
import { HomePage } from '@/app/HomePage';
import { exampleRoutes } from '@/features/examples/routes';
import { PageLayout } from '@/ui';

export const router = createBrowserRouter([
  {
    element: <PageLayout />,
    children: [{ index: true, element: <HomePage /> }, ...exampleRoutes],
  },
]);
