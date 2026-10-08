// Vitest setup: jest-dom matchers, one MSW server for every test file, and a render helper with
// a fresh QueryClient per test. Unhandled requests fail the test instead of hitting the network.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { setupServer } from 'msw/node';
import { afterAll, afterEach, beforeAll } from 'vitest';

export { renderWithProviders } from './render';

export const server = setupServer();

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => {
  cleanup();
  server.resetHandlers();
});
afterAll(() => server.close());
