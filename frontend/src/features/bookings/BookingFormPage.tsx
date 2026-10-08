// The customer's booking page: wash programme, day, time, name and licence plate.
// useApiForm validates with the generated Zod schema before sending and puts the server's field
// errors on the fields. On success: confirm and go to the confirmation page.
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router';
import { getGetBookingQueryKey, useCreateBooking } from '@/api/generated/bookings/bookings';
import { CreateBookingBody } from '@/api/generated/bookings/bookings.zod';
import type { CreateBookingRequest } from '@/api/generated/model';
import { useApiForm } from '@/platform/forms/useApiForm';
import { Button, Form, Input, PageTitle, Select, useToast } from '@/ui';
import { dateOptions, FIRST_TIME_SLOT, firstBookableDate, TIME_OPTIONS } from './components/appointment';
import { WASH_PROGRAMME_OPTIONS } from './components/washProgramme';

export function BookingFormPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();
  const days = dateOptions();
  const empty: CreateBookingRequest = {
    washProgramme: 'BASIC',
    appointmentDate: firstBookableDate(),
    appointmentTime: FIRST_TIME_SLOT,
    customerName: '',
    licencePlate: '',
  };
  const form = useApiForm(CreateBookingBody, empty);
  const { errors, isSubmitting } = form.formState;

  const create = useCreateBooking({
    mutation: {
      onSuccess: async (response) => {
        const booking = response.data;
        await queryClient.invalidateQueries({ queryKey: getGetBookingQueryKey(booking.id) });
        toast.show('Termin gebucht.');
        await navigate(`/bookings/${booking.id}`);
      },
      onError: form.handleServerError,
    },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    await create.mutateAsync({ data: values }).catch(() => undefined);
  });

  return (
    <section>
      <PageTitle>Waschtermin buchen</PageTitle>
      <Form
        label="Waschtermin buchen"
        onSubmit={(event) => void onSubmit(event)}
        error={form.formError}
        actions={
          <Button type="submit" disabled={isSubmitting}>
            Termin buchen
          </Button>
        }
      >
        <Select
          label="Waschprogramm"
          required
          options={WASH_PROGRAMME_OPTIONS}
          error={errors.washProgramme?.message}
          {...form.register('washProgramme')}
        />
        <Select
          label="Datum"
          required
          options={days}
          error={errors.appointmentDate?.message}
          {...form.register('appointmentDate')}
        />
        <Select
          label="Uhrzeit"
          required
          options={TIME_OPTIONS}
          error={errors.appointmentTime?.message}
          {...form.register('appointmentTime')}
        />
        <Input
          label="Name"
          required
          error={errors.customerName?.message}
          {...form.register('customerName')}
        />
        <Input
          label="Autokennzeichen"
          required
          error={errors.licencePlate?.message}
          {...form.register('licencePlate')}
        />
      </Form>
    </section>
  );
}
