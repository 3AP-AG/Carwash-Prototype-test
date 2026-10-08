// ui/error-state: one error presentation for every feature (MUI Alert, role "alert"); the message
// comes from the ApiError's ProblemDetail. Status colours are for messages like this one only.
import Alert from '@mui/material/Alert';

export interface ErrorStateProps {
  message?: string;
}

export function ErrorState({ message = 'Something went wrong.' }: ErrorStateProps) {
  return (
    <Alert severity="error" variant="outlined">
      {message}
    </Alert>
  );
}
