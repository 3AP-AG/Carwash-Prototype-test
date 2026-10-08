// Detail exemplar: the generated useGetExample hook; a 404 from the API becomes "does not exist",
// anything else the generic error state.
import { useParams } from 'react-router';
import { ApiError } from '@/api/client';
import { useGetExample } from '@/api/generated/examples/examples';
import { Button, Card, CardField, ErrorState, LoadingState, PageTitle } from '@/ui';
import { STATUS_LABELS } from './components/status';

export function ExampleDetailPage() {
  const { id = '' } = useParams();
  const { data, isPending, isError, error } = useGetExample(id);

  if (isPending) {
    return <LoadingState />;
  }
  if (isError) {
    const notFound = error instanceof ApiError && error.status === 404;
    return (
      <section>
        <ErrorState
          message={notFound ? 'This example does not exist.' : 'The example could not be loaded.'}
        />
        <Button variant="text" to="/examples">
          Back to the list
        </Button>
      </section>
    );
  }

  const example = data.data;
  return (
    <section>
      <PageTitle>{example.title}</PageTitle>
      <Card>
        <CardField label="Status">{STATUS_LABELS[example.status]}</CardField>
        <CardField label="Description">{example.description || '—'}</CardField>
        <CardField label="Created">{new Date(example.createdAt).toLocaleString()}</CardField>
        <CardField label="Last changed">{new Date(example.updatedAt).toLocaleString()}</CardField>
      </Card>
      <Button to={`/examples/${example.id}/edit`}>Edit</Button>
      <Button variant="text" to="/examples">
        Back to the list
      </Button>
    </section>
  );
}
