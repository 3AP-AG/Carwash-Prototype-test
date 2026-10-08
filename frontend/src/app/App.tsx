// App shell: the theme, the one QueryClient and the router. Nothing feature-specific lives here.
import { QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router';
import { queryClient } from '@/app/queryClient';
import { router } from '@/app/router';
import { UiProvider } from '@/ui';

export function App() {
  return (
    <UiProvider>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </UiProvider>
  );
}
