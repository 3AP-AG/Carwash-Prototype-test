// ui/page-layout: the app frame around every route.
import Container from '@mui/material/Container';
import { Outlet } from 'react-router';

export function PageLayout() {
  return (
    <Container component="main" maxWidth="lg" sx={{ py: 8 }}>
      <Outlet />
    </Container>
  );
}
