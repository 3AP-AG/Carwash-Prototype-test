// Acceptance test for D-01: a customer books a wash appointment.
// One test per acceptance bullet. Creates its own data with a unique name, so runs never depend
// on each other.
import { expect, test } from '@playwright/test';

test('the booking page offers the wash programmes with their price', { tag: '@D-01' }, async ({
  page,
}) => {
  await page.goto('/');

  const programme = page.getByLabel('Waschprogramm');
  await expect(programme).toBeVisible();
  await expect(programme.getByRole('option')).toHaveText([
    'Basiswäsche – CHF 12.–',
    'Komfortwäsche – CHF 19.–',
    'Premiumwäsche – CHF 29.–',
  ]);
});

test('a customer picks a date and a time and enters name and licence plate', { tag: '@D-01' }, async ({
  page,
}) => {
  await page.goto('/');

  await expect(page.getByLabel('Datum')).toBeVisible();
  await expect(page.getByLabel('Uhrzeit')).toBeVisible();
  await page.getByLabel('Name').fill('Anna Muster');
  await page.getByLabel('Autokennzeichen').fill('ZH 123456');

  await expect(page.getByLabel('Name')).toHaveValue('Anna Muster');
  await expect(page.getByLabel('Autokennzeichen')).toHaveValue('ZH 123456');
});

test('a missing name or licence plate keeps the customer on the page', { tag: '@D-01' }, async ({
  page,
}) => {
  await page.goto('/');

  await page.getByLabel('Autokennzeichen').fill('ZH 123456');
  await page.getByRole('button', { name: 'Termin buchen' }).click();

  await expect(page.getByText('Bitte geben Sie Ihren Namen ein.')).toBeVisible();
  await expect(page.getByLabel('Name')).toHaveAttribute('aria-invalid', 'true');
  await expect(page).toHaveURL(/\/$/);

  await page.getByLabel('Name').fill('Anna Muster');
  await page.getByLabel('Autokennzeichen').fill('');
  await page.getByRole('button', { name: 'Termin buchen' }).click();

  await expect(page.getByText('Bitte geben Sie Ihr Autokennzeichen ein.')).toBeVisible();
  await expect(page).toHaveURL(/\/$/);
});

test('a booked appointment is confirmed with programme, date and time', { tag: '@D-01' }, async ({
  page,
}) => {
  const name = `Anna Muster ${Date.now()}`;

  await page.goto('/');
  await page.getByLabel('Waschprogramm').selectOption('PREMIUM');
  const date = await page.getByLabel('Datum').inputValue();
  await page.getByLabel('Uhrzeit').selectOption('10:30:00');
  await page.getByLabel('Name').fill(name);
  await page.getByLabel('Autokennzeichen').fill('ZH 123456');
  await page.getByRole('button', { name: 'Termin buchen' }).click();

  const [year, month, day] = date.split('-');
  await expect(page.getByRole('heading', { name: 'Ihr Termin ist gebucht' })).toBeVisible();
  await expect(page.getByText(/Premiumwäsche/)).toBeVisible();
  await expect(page.getByText(new RegExp(`${day}\\.${month}\\.${year}`))).toBeVisible();
  await expect(page.getByText('10:30 Uhr')).toBeVisible();
  await expect(page.getByText(name)).toBeVisible();
  await expect(page).toHaveURL(/\/bookings\/[0-9a-f-]{36}$/);
});
