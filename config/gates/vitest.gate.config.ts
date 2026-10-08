// Gate: frontend tests. Locked. Takes plugins and aliases from frontend/vite.config.ts, then fixes
// everything that decides WHAT runs: include/exclude, environment, setup, no passing without tests.
// No package imports here: packages resolve from this file's folder, which has no node_modules.
// TODO(CAAS-1383): failures-only reporting (config/gates/reporters/).
import { fileURLToPath } from 'node:url';
import viteConfig from '../../frontend/vite.config.ts';

const root = fileURLToPath(new URL('../../frontend', import.meta.url));

export default {
  ...viteConfig,
  root,
  test: {
    include: ['src/**/*.test.{ts,tsx}'],
    exclude: ['**/node_modules/**'],
    environment: 'jsdom',
    setupFiles: ['src/test/setup.ts'],
    passWithNoTests: false,
    allowOnly: false,
  },
};
