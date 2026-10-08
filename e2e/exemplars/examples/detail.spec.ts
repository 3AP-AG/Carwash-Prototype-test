// Exemplar acceptance test for the detail page. Copy its shape; never change it to make a run pass.
// Runs against seeded data: the local seed locally, the candidate's data in CI.
import { expect, test } from '@playwright/test';

test('an example opens from the list and shows its fields', { tag: '@exemplar' }, async ({ page }) => {
  await page.goto('/examples');

  const first = page.getByRole('table', { name: 'Examples' }).getByRole('link').first();
  const title = await first.textContent();
  await first.click();

  await expect(page.getByRole('heading', { name: title ?? '' })).toBeVisible();
  await expect(page.getByText('Status')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Edit' })).toBeVisible();
});

test('an unknown example says it does not exist', { tag: '@exemplar' }, async ({ page }) => {
  await page.goto('/examples/00000000-0000-0000-0000-000000000000');

  await expect(page.getByRole('alert')).toHaveText('This example does not exist.');
});
