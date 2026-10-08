// api/openapi.yaml → src/api/generated/: TanStack Query hooks, Zod schemas, MSW handlers.
// One file per OpenAPI tag (per feature): examples.ts, examples.zod.ts, examples.msw.ts. Run by `make generate`.
import { defineConfig } from 'orval';

const input = { target: '../api/openapi.yaml' };

export default defineConfig({
  client: {
    input,
    output: {
      mode: 'tags-split',
      target: 'src/api/generated',
      schemas: 'src/api/generated/model',
      client: 'react-query',
      httpClient: 'fetch',
      clean: true,
      mock: { generators: [{ type: 'msw' }] },
      override: {
        mutator: { path: 'src/api/client.ts', name: 'apiFetch' },
      },
    },
  },
  zod: {
    input,
    output: {
      mode: 'tags-split',
      target: 'src/api/generated',
      client: 'zod',
      fileExtension: '.zod.ts',
    },
  },
});
