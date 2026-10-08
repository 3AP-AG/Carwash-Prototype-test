// ui/link: a text link to a route of this app.
import MuiLink from '@mui/material/Link';
import type { ReactNode } from 'react';
import { Link as RouterLink } from 'react-router';

export interface LinkProps {
  to: string;
  children: ReactNode;
}

export function Link({ to, children }: LinkProps) {
  return (
    <MuiLink component={RouterLink} to={to}>
      {children}
    </MuiLink>
  );
}
