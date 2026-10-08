import { screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router';
import { describe, expect, it } from 'vitest';
import { getGetExampleMockHandler, getGetExampleResponseMock } from '@/api/generated/examples/examples.msw';
import { renderWithProviders, server } from '@/test/setup';
import { ExampleDetailPage } from './ExampleDetailPage';

const ID = '00000000-0000-0000-0000-000000000001';

function renderDetail() {
  renderWithProviders(
    <Routes>
      <Route path="/examples/:id" element={<ExampleDetailPage />} />
    </Routes>,
    { route: `/examples/${ID}` },
  );
}

describe('ExampleDetailPage', () => {
  it('shows the example', async () => {
    server.use(
      getGetExampleMockHandler(
        getGetExampleResponseMock({
          id: ID,
          title: 'First example',
          status: 'IN_PROGRESS',
          description: 'Details.',
        }),
      ),
    );

    renderDetail();

    expect(await screen.findByRole('heading', { name: 'First example' })).toBeInTheDocument();
    expect(screen.getByText('In progress')).toBeInTheDocument();
    expect(screen.getByText('Details.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Edit' })).toHaveAttribute('href', `/examples/${ID}/edit`);
  });

  it('says so when the example does not exist', async () => {
    server.use(
      http.get('*/api/examples/:id', () =>
        HttpResponse.json(
          { status: 404, title: 'Not Found', detail: 'Example does not exist.' },
          { status: 404 },
        ),
      ),
    );

    renderDetail();

    expect(await screen.findByRole('alert')).toHaveTextContent('This example does not exist.');
  });
});
