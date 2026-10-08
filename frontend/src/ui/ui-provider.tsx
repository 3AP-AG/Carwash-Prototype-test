// ui/ui-provider: wraps the app (and every component test) in the 3ap theme and the toast area.
// CssBaseline sets the page background, text colour and body typography from the theme.
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider } from '@mui/material/styles';
import type { ReactNode } from 'react';
import { theme } from './theme';
import { ToastProvider } from './toast';

export function UiProvider({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <ToastProvider>{children}</ToastProvider>
    </ThemeProvider>
  );
}
