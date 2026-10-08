// Routes of the examples feature. Registered in app/router.tsx but never linked in the navigation:
// the exemplar must not show up in a demo.
import type { RouteObject } from 'react-router';
import { ExampleDetailPage } from './ExampleDetailPage';
import { ExampleFormPage } from './ExampleFormPage';
import { ExampleListPage } from './ExampleListPage';

export const exampleRoutes: RouteObject[] = [
  { path: 'examples', element: <ExampleListPage /> },
  { path: 'examples/new', element: <ExampleFormPage /> },
  { path: 'examples/:id', element: <ExampleDetailPage /> },
  { path: 'examples/:id/edit', element: <ExampleFormPage /> },
];
