// Exemplar acceptance test for the form page. Copy its shape; never change it to make a run pass.
// Creates its own data with a unique title, so runs never depend on each other.
import { expect, test } from '@playwright/test';

test('a new example can be created and then edited', { tag: '@exemplar' }, async ({ page }) => {
  const title = `E2E example ${Date.now()}`;

  await page.goto('/examples');
  await page.getByRole('link', { name: 'New example' }).click();
  await page.getByLabel('Title').fill(title);
  await page.getByLabel('Status').selectOption('IN_PROGRESS');
  await page.getByLabel('Description').fill('Created by the acceptance test.');
  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByRole('heading', { name: title })).toBeVisible();
  await expect(page.getByText('In progress')).toBeVisible();

  await page.getByRole('link', { name: 'Edit' }).click();
  await page.getByLabel('Status').selectOption('CLOSED');
  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByRole('heading', { name: title })).toBeVisible();
  await expect(page.getByText('Closed')).toBeVisible();
});

test('a blank title is rejected with a message on the field', { tag: '@exemplar' }, async ({ page }) => {
  await page.goto('/examples/new');
  await page.getByLabel('Title').fill('   ');
  await page.getByRole('button', { name: 'Save' }).click();

  await expect(page.getByLabel('Title')).toHaveAttribute('aria-invalid', 'true');
  await expect(page).toHaveURL(/\/examples\/new$/);
});
