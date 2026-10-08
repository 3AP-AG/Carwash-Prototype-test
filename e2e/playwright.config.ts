// Locked. Chromium only. baseURL is the candidate URL in CI (CAAS-1389) and the local
// container (`make up`) on a laptop. auth.setup.ts logs in once per run; every test reuses it.
// Screenshots per acceptance item are CAAS-1391's; failures-only reporting is CAAS-1383's.
import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.BASE_URL ?? 'http://localhost:8080';

export default defineConfig({
  testDir: '.',
  forbidOnly: true,
  retries: 0,
  reporter: 'list',
  use: { baseURL, trace: 'retain-on-failure' },
  projects: [
    { name: 'setup', testMatch: /auth\.setup\.ts/ },
    {
      name: 'chromium',
      testMatch: /\.spec\.ts$/,
      dependencies: ['setup'],
      use: { ...devices['Desktop Chrome'], storageState: '.auth/state.json' },
    },
  ],
});
