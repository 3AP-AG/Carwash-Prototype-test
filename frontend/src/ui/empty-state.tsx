// ui/empty-state: what a list shows when it has nothing to show.
import Typography from '@mui/material/Typography';

export interface EmptyStateProps {
  message: string;
}

export function EmptyState({ message }: EmptyStateProps) {
  return (
    <Typography color="text.secondary" sx={{ py: 4 }}>
      {message}
    </Typography>
  );
}
