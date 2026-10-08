// ui/loading-state: spinner plus label, announced politely to screen readers.
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import Typography from '@mui/material/Typography';

export interface LoadingStateProps {
  label?: string;
}

export function LoadingState({ label = 'Loading…' }: LoadingStateProps) {
  return (
    <Box role="status" aria-live="polite" sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 4 }}>
      <CircularProgress size={20} aria-hidden />
      <Typography>{label}</Typography>
    </Box>
  );
}
