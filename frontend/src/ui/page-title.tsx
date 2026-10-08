// ui/page-title: the one heading per page (an <h1>, styled as the theme's page-title variant).
import Typography from '@mui/material/Typography';

export interface PageTitleProps {
  children: string;
}

export function PageTitle({ children }: PageTitleProps) {
  return (
    <Typography variant="h4" component="h1" gutterBottom>
      {children}
    </Typography>
  );
}
