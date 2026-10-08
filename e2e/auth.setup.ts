// Signs in once per run through the real entry page (Spring-rendered, outside the bundle) and saves
// the session for every test. Login mode when E2E_USERNAME is set, passcode mode otherwise. In CI
// the credential is the run-scoped one the pipeline creates for this run only (CAAS-1381); locally
// the local seed's (passcode `local-passcode`; user `local` / `local-password`).
import { expect, test as setup } from '@playwright/test';

const { E2E_USERNAME: username, E2E_PASSWORD: password, E2E_PASSCODE: passcode } = process.env;

setup('sign in once', async ({ page }) => {
  await page.goto('/');
  if (username) {
    if (!password) {
      throw new Error('E2E_USERNAME is set but E2E_PASSWORD is not');
    }
    await expect(page).toHaveURL(/\/login$/);
    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Password').fill(password);
    await page.getByRole('button', { name: 'Sign in' }).click();
    await expect(page).not.toHaveURL(/\/login/);
  } else {
    if (!passcode) {
      throw new Error('Set E2E_PASSCODE, or E2E_USERNAME and E2E_PASSWORD');
    }
    await expect(page).toHaveURL(/\/gate$/);
    await page.getByLabel('Passcode').fill(passcode);
    await page.getByRole('button', { name: 'Enter' }).click();
    await expect(page).not.toHaveURL(/\/gate$/);
  }
  await page.context().storageState({ path: '.auth/state.json' });
});
