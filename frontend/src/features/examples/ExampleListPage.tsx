// List exemplar: the generated useListExamples hook, loading / error / empty states and pagination
// from @/ui. The page number lives in the URL (?page=), so back/forward and shared links keep it.
// A page past the end (a stale link after deletions) says so and links back to the first page.
import { useSearchParams } from 'react-router';
import { useListExamples } from '@/api/generated/examples/examples';
import type { ExampleSummaryResponse } from '@/api/generated/model';
import { Button, EmptyState, ErrorState, Link, LoadingState, PageTitle, Pagination, Table } from '@/ui';
import type { TableColumn } from '@/ui';
import { STATUS_LABELS } from './components/status';

const PAGE_SIZE = 20;

const columns: TableColumn<ExampleSummaryResponse>[] = [
  { key: 'title', header: 'Title', render: (row) => <Link to={`/examples/${row.id}`}>{row.title}</Link> },
  { key: 'status', header: 'Status', render: (row) => STATUS_LABELS[row.status] },
  { key: 'createdAt', header: 'Created', render: (row) => new Date(row.createdAt).toLocaleDateString() },
];

export function ExampleListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parsePage(searchParams.get('page'));
  const { data, isPending, isError } = useListExamples({ page, size: PAGE_SIZE });

  return (
    <section>
      <PageTitle>Examples</PageTitle>
      <Button to="/examples/new">New example</Button>
      {isPending ? (
        <LoadingState />
      ) : isError ? (
        <ErrorState message="The examples could not be loaded." />
      ) : data.data.totalItems === 0 ? (
        <EmptyState message="No examples yet." />
      ) : data.data.items.length === 0 ? (
        <>
          <EmptyState message="This page has no examples." />
          <Link to="/examples">Go to the first page</Link>
        </>
      ) : (
        <>
          <Table caption="Examples" columns={columns} rows={data.data.items} rowKey={(row) => row.id} />
          <Pagination
            page={data.data.page}
            size={data.data.size}
            totalItems={data.data.totalItems}
            onPageChange={(next) => setSearchParams(next === 0 ? {} : { page: String(next) })}
          />
        </>
      )}
    </section>
  );
}

/** ?page= as a page index: a non-negative integer, anything else is the first page. */
function parsePage(value: string | null): number {
  const page = Number(value ?? 0);
  return Number.isInteger(page) && page > 0 ? page : 0;
}
