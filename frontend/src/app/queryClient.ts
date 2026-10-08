// The one TanStack QueryClient. A 401 means the session ended (expired or revoked passcode, expired
// user): reload the page, so Spring sends the browser to the mode's entry page (/gate or /login,
// served outside this bundle) and back to this page afterwards. Under `make dev`, Vite serves the
// page without asking Spring, so a reload would loop: go to the entry page (proxied) instead.
import { MutationCache, QueryCache, QueryClient } from '@tanstack/react-query';
import { ApiError } from '@/api/client';

function onError(error: unknown) {
  if (error instanceof ApiError && error.status === 401) {
    if (import.meta.env.DEV) {
      window.location.assign(import.meta.env.VITE_ENTRY_PAGE);
    } else {
      window.location.reload();
    }
  }
}

export function createQueryClient() {
  return new QueryClient({
    queryCache: new QueryCache({ onError }),
    mutationCache: new MutationCache({ onError }),
    defaultOptions: {
      queries: {
        retry: (failureCount, error) => !(error instanceof ApiError) && failureCount < 2,
        staleTime: 30_000,
      },
    },
  });
}

export const queryClient = createQueryClient();
