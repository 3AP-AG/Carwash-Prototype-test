// Vite config. Dev server proxies /api and both entry pages (/gate, /login) to Spring on :8080, so
// local dev is same-origin like production. VITE_ENTRY_PAGE is where a 401 sends the browser in
// dev (app/queryClient), from the same PROTECTION_MODE `make dev` gives Spring. Test settings live
// in the locked config/gates/vitest.gate.config.ts.
import { fileURLToPath } from 'node:url';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// changeOrigin off (the string shorthand turns it on): Spring sees the Vite host, so its redirects
// (after sign-in, to the entry page) stay on the dev server instead of jumping to :8080.
const backend = { target: 'http://localhost:8080', changeOrigin: false };
const entryPage = process.env.PROTECTION_MODE === 'login' ? '/login' : '/gate';

export default defineConfig({
  plugins: [react()],
  define: {
    'import.meta.env.VITE_ENTRY_PAGE': JSON.stringify(entryPage),
  },
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  server: {
    proxy: {
      '/api': backend,
      '/gate': backend,
      '/login': backend,
    },
  },
});
