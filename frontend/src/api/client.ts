// The only hand-written file in api/: the fetcher every generated hook calls (orval mutator).
// Same origin as the backend, so paths stay relative and the session cookie goes along.
// Non-2xx → ApiError carrying the ProblemDetail. Mutations send Spring's CSRF header.

export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  errors?: { field: string; message: string }[];
}

export class ApiError extends Error {
  readonly problem: ProblemDetail;

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `Request failed with ${problem.status}`);
    this.name = 'ApiError';
    this.problem = problem;
  }

  get status(): number {
    return this.problem.status;
  }
}

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

function csrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    ?.slice('XSRF-TOKEN='.length);
}

export async function apiFetch<T>(url: string, options: RequestInit = {}): Promise<T> {
  const method = (options.method ?? 'GET').toUpperCase();
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');
  const token = SAFE_METHODS.has(method) ? undefined : csrfToken();
  if (token) {
    headers.set('X-XSRF-TOKEN', decodeURIComponent(token));
  }

  // Resolve against the page's origin: same request in the browser, and works in jsdom tests.
  const absoluteUrl = new URL(url, window.location.origin);
  const response = await fetch(absoluteUrl, { ...options, method, headers, credentials: 'same-origin' });
  const body: unknown = response.status === 204 ? undefined : await response.json().catch(() => undefined);

  if (!response.ok) {
    const problem = (body as ProblemDetail | undefined) ?? { status: response.status };
    throw new ApiError({ ...problem, status: response.status });
  }
  return { data: body, status: response.status, headers: response.headers } as T;
}
