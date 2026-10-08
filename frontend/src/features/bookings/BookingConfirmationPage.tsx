// What the customer sees right after booking: the generated useGetBooking hook; a 404 from the API
// becomes "existiert nicht", anything else the generic error state.
import { useParams } from 'react-router';
import { ApiError } from '@/api/client';
import { useGetBooking } from '@/api/generated/bookings/bookings';
import { Button, Card, CardField, ErrorState, LoadingState, PageTitle } from '@/ui';
import { formatDate, formatTime } from './components/appointment';
import { WASH_PROGRAMME_LABELS, washProgrammePrice } from './components/washProgramme';

export function BookingConfirmationPage() {
  const { id = '' } = useParams();
  const { data, isPending, isError, error } = useGetBooking(id);

  if (isPending) {
    return <LoadingState />;
  }
  if (isError) {
    const notFound = error instanceof ApiError && error.status === 404;
    return (
      <section>
        <ErrorState
          message={notFound ? 'Diese Buchung existiert nicht.' : 'Die Buchung konnte nicht geladen werden.'}
        />
        <Button variant="text" to="/">
          Zur Buchung
        </Button>
      </section>
    );
  }

  const booking = data.data;
  return (
    <section>
      <PageTitle>Ihr Termin ist gebucht</PageTitle>
      <Card>
        <CardField label="Waschprogramm">
          {WASH_PROGRAMME_LABELS[booking.washProgramme]} – {washProgrammePrice(booking.washProgramme)}
        </CardField>
        <CardField label="Datum">{formatDate(booking.appointmentDate)}</CardField>
        <CardField label="Uhrzeit">{formatTime(booking.appointmentTime)} Uhr</CardField>
        <CardField label="Name">{booking.customerName}</CardField>
        <CardField label="Autokennzeichen">{booking.licencePlate}</CardField>
      </Card>
      <Button to="/">Weiteren Termin buchen</Button>
    </section>
  );
}
