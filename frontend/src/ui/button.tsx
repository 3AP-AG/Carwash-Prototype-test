// ui/button: the one button. `to` makes it a link to a route (same look, real <a>), so features
// never wire router links into MUI themselves.
import MuiButton from '@mui/material/Button';
import type { ReactNode } from 'react';
import { Link as RouterLink } from 'react-router';

export interface ButtonProps {
  children: ReactNode;
  variant?: 'primary' | 'secondary' | 'text';
  type?: 'button' | 'submit';
  to?: string;
  onClick?: () => void;
  disabled?: boolean;
}

const MUI_VARIANT = { primary: 'contained', secondary: 'outlined', text: 'text' } as const;

export function Button({
  children,
  variant = 'primary',
  type = 'button',
  to,
  onClick,
  disabled,
}: ButtonProps) {
  const muiVariant = MUI_VARIANT[variant];
  if (to) {
    return (
      <MuiButton component={RouterLink} to={to} variant={muiVariant} disabled={disabled}>
        {children}
      </MuiButton>
    );
  }
  return (
    <MuiButton type={type} variant={muiVariant} onClick={onClick} disabled={disabled}>
      {children}
    </MuiButton>
  );
}
