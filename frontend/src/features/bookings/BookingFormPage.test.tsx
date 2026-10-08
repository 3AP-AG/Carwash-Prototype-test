import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router';
import { describe, expect, it } from 'vitest';
import {
  getCreateBookingMockHandler,
  getCreateBookingResponseMock,
} from '@/api/generated/bookings/bookings.msw';
import { renderWithProviders, server } from '@/test/setup';
import { BookingFormPage } from './BookingFormPage';
import { dateOptions } from './components/appointment';

const ID = '00000000-0000-0000-0000-000000000001';

function renderForm() {
  renderWithProviders(
    <Routes>
      <Route path="/" element={<BookingFormPage />} />
      <Route path="/bookings/:id" element={<p>Bestätigung</p>} />
    </Routes>,
    { route: '/' },
  );
}

describe('BookingFormPage', () => {
  it('offers the wash programmes with their price', () => {
    renderForm();

    expect(screen.getByRole('option', { name: 'Basiswäsche – CHF 12.–' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Komfortwäsche – CHF 19.–' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Premiumwäsche – CHF 29.–' })).toBeInTheDocument();
  });

  it('books the appointment and goes to the confirmation', async () => {
    let sent: unknown;
    server.use(
      getCreateBookingMockHandler(async ({ request }) => {
        sent = await request.json();
        return getCreateBookingResponseMock({ id: ID });
      }),
    );
    const user = userEvent.setup();
    renderForm();

    const day = dateOptions()[1]?.value ?? '';
    await user.selectOptions(screen.getByLabelText(/Waschprogramm/), 'PREMIUM');
    await user.selectOptions(screen.getByLabelText(/Datum/), day);
    await user.selectOptions(screen.getByLabelText(/Uhrzeit/), '10:30:00');
    await user.type(screen.getByLabelText(/Name/), 'Anna Muster');
    await user.type(screen.getByLabelText(/Autokennzeichen/), 'ZH 123456');
    await user.click(screen.getByRole('button', { name: 'Termin buchen' }));

    expect(await screen.findByText('Bestätigung')).toBeInTheDocument();
    expect(screen.getByText('Termin gebucht.')).toBeInTheDocument();
    expect(sent).toEqual({
      washProgramme: 'PREMIUM',
      appointmentDate: day,
      appointmentTime: '10:30:00',
      customerName: 'Anna Muster',
      licencePlate: 'ZH 123456',
    });
  });

  it('shows the server field errors on the fields', async () => {
    server.use(
      http.post('*/api/bookings', () =>
        HttpResponse.json(
          {
            status: 400,
            title: 'Bad Request',
            errors: [{ field: 'customerName', message: 'Bitte geben Sie Ihren Namen ein.' }],
          },
          { status: 400 },
        ),
      ),
    );
    const user = userEvent.setup();
    renderForm();

    await user.type(screen.getByLabelText(/Autokennzeichen/), 'ZH 123456');
    await user.click(screen.getByRole('button', { name: 'Termin buchen' }));

    expect(await screen.findByText('Bitte geben Sie Ihren Namen ein.')).toBeInTheDocument();
    expect(screen.getByLabelText(/Name/)).toHaveAttribute('aria-invalid', 'true');
    expect(screen.queryByText('Bestätigung')).not.toBeInTheDocument();
  });
});
