// Form exemplar: create (/examples/new) and edit (/examples/:id/edit) in one page.
// useApiForm validates with the generated Zod schema before sending and puts the server's field
// errors on the fields. On success: refresh the cached list and detail, confirm, go to the detail.
// An edit sends the version it was based on; if someone saved in between, the server answers 409
// and its message becomes the form error.
import { useQueryClient } from '@tanstack/react-query';
import { useNavigate, useParams } from 'react-router';
import {
  getGetExampleQueryKey,
  getListExamplesQueryKey,
  useCreateExample,
  useGetExample,
  useUpdateExample,
} from '@/api/generated/examples/examples';
import { CreateExampleBody } from '@/api/generated/examples/examples.zod';
import type { CreateExampleRequest } from '@/api/generated/model';
import { useApiForm } from '@/platform/forms/useApiForm';
import { Button, ErrorState, Form, Input, LoadingState, PageTitle, Select, Textarea, useToast } from '@/ui';
import { STATUS_OPTIONS } from './components/status';

const EMPTY: CreateExampleRequest = { title: '', status: 'OPEN', description: '' };

export function ExampleFormPage() {
  const { id } = useParams();
  const existing = useGetExample(id ?? '', { query: { enabled: Boolean(id) } });

  if (!id) {
    return <ExampleForm initial={EMPTY} />;
  }
  if (existing.isPending) {
    return <LoadingState />;
  }
  if (existing.isError) {
    return <ErrorState message="The example could not be loaded." />;
  }
  const { title, status, description, version } = existing.data.data;
  return <ExampleForm editing={{ id, version }} initial={{ title, status, description }} />;
}

interface Editing {
  id: string;
  version: number;
}

function ExampleForm({ editing, initial }: { editing?: Editing; initial: CreateExampleRequest }) {
  const id = editing?.id;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const toast = useToast();
  const form = useApiForm(CreateExampleBody, initial);
  const { errors, isSubmitting } = form.formState;

  const onSaved = async (savedId: string) => {
    await queryClient.invalidateQueries({ queryKey: getListExamplesQueryKey() });
    await queryClient.invalidateQueries({ queryKey: getGetExampleQueryKey(savedId) });
    toast.show('Example saved.');
    await navigate(`/examples/${savedId}`);
  };
  const create = useCreateExample({
    mutation: { onSuccess: (response) => onSaved(response.data.id), onError: form.handleServerError },
  });
  const update = useUpdateExample({
    mutation: { onSuccess: (response) => onSaved(response.data.id), onError: form.handleServerError },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    if (editing) {
      const data = { ...values, version: editing.version };
      await update.mutateAsync({ id: editing.id, data }).catch(() => undefined);
    } else {
      await create.mutateAsync({ data: values }).catch(() => undefined);
    }
  });

  return (
    <section>
      <PageTitle>{id ? 'Edit example' : 'New example'}</PageTitle>
      <Form
        label={id ? 'Edit example' : 'New example'}
        onSubmit={(event) => void onSubmit(event)}
        error={form.formError}
        actions={
          <>
            <Button type="submit" disabled={isSubmitting}>
              Save
            </Button>
            <Button variant="text" to={id ? `/examples/${id}` : '/examples'}>
              Cancel
            </Button>
          </>
        }
      >
        <Input label="Title" required error={errors.title?.message} {...form.register('title')} />
        <Select
          label="Status"
          required
          options={STATUS_OPTIONS}
          error={errors.status?.message}
          {...form.register('status')}
        />
        <Textarea label="Description" error={errors.description?.message} {...form.register('description')} />
      </Form>
    </section>
  );
}
