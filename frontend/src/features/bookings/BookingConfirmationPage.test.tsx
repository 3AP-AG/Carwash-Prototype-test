import { screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router';
import { describe, expect, it } from 'vitest';
import { getGetBookingMockHandler, getGetBookingResponseMock } from '@/api/generated/bookings/bookings.msw';
import { renderWithProviders, server } from '@/test/setup';
import { BookingConfirmationPage } from './BookingConfirmationPage';

const ID = '00000000-0000-0000-0000-000000000001';

function renderConfirmation() {
  renderWithProviders(
    <Routes>
      <Route path="/bookings/:id" element={<BookingConfirmationPage />} />
    </Routes>,
    { route: `/bookings/${ID}` },
  );
}

describe('BookingConfirmationPage', () => {
  it('shows the booked appointment', async () => {
    server.use(
      getGetBookingMockHandler(
        getGetBookingResponseMock({
          id: ID,
          washProgramme: 'PREMIUM',
          appointmentDate: '2026-11-03',
          appointmentTime: '10:30:00',
          customerName: 'Anna Muster',
          licencePlate: 'ZH 123456',
        }),
      ),
    );

    renderConfirmation();

    expect(await screen.findByRole('heading', { name: 'Ihr Termin ist gebucht' })).toBeInTheDocument();
    expect(screen.getByText(/Premiumwäsche/)).toBeInTheDocument();
    expect(screen.getByText(/CHF 29/)).toBeInTheDocument();
    expect(screen.getByText(/03\.11\.2026/)).toBeInTheDocument();
    expect(screen.getByText('10:30 Uhr')).toBeInTheDocument();
    expect(screen.getByText('Anna Muster')).toBeInTheDocument();
    expect(screen.getByText('ZH 123456')).toBeInTheDocument();
  });

  it('says so when the booking does not exist', async () => {
    server.use(
      http.get('*/api/bookings/:id', () =>
        HttpResponse.json(
          { status: 404, title: 'Not Found', detail: 'Booking does not exist.' },
          { status: 404 },
        ),
      ),
    );

    renderConfirmation();

    expect(await screen.findByRole('alert')).toHaveTextContent('Diese Buchung existiert nicht.');
  });
});
