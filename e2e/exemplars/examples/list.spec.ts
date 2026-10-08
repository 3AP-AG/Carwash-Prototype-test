// Exemplar acceptance test for the list page. Copy its shape for e2e/backlog/<ID>.spec.ts: one test
// per acceptance bullet, tagged with its ID, roles over CSS. Never change it to make a run pass.
// Runs against seeded data: the local seed locally, the candidate's data in CI.
import { expect, test } from '@playwright/test';

test('the examples list shows the existing examples', { tag: '@exemplar' }, async ({ page }) => {
  await page.goto('/examples');

  const table = page.getByRole('table', { name: 'Examples' });
  await expect(table).toBeVisible();
  await expect(table.getByRole('row')).not.toHaveCount(1); // more than the header row
});
