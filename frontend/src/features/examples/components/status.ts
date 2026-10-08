// Display labels for ExampleStatus, in one place for the list, the detail view and the form.
import type { ExampleSummaryResponseStatus } from '@/api/generated/model';

export const STATUS_LABELS: Record<ExampleSummaryResponseStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In progress',
  CLOSED: 'Closed',
};

export const STATUS_OPTIONS = Object.entries(STATUS_LABELS).map(([value, label]) => ({ value, label }));
