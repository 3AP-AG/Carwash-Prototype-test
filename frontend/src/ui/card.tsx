// ui/card: a framed block of content, e.g. the fields of a detail view (CardField).
import MuiCard from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';
import type { ReactNode } from 'react';

export interface CardProps {
  title?: string;
  children: ReactNode;
}

export function Card({ title, children }: CardProps) {
  return (
    <MuiCard variant="outlined">
      <CardContent>
        {title && (
          <Typography variant="h5" component="h2" gutterBottom>
            {title}
          </Typography>
        )}
        <Stack component="dl" spacing={4} sx={{ m: 0 }}>
          {children}
        </Stack>
      </CardContent>
    </MuiCard>
  );
}

export interface CardFieldProps {
  label: string;
  children: ReactNode;
}

/** One label/value pair inside a Card. */
export function CardField({ label, children }: CardFieldProps) {
  return (
    <div>
      <Typography component="dt" variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography component="dd" sx={{ m: 0 }}>
        {children}
      </Typography>
    </div>
  );
}
