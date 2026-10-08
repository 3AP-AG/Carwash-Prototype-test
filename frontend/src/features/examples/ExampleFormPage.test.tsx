import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { Route, Routes } from 'react-router';
import { describe, expect, it } from 'vitest';
import {
  getCreateExampleMockHandler,
  getCreateExampleResponseMock,
  getGetExampleMockHandler,
  getGetExampleResponseMock,
  getUpdateExampleMockHandler,
} from '@/api/generated/examples/examples.msw';
import { renderWithProviders, server } from '@/test/setup';
import { ExampleFormPage } from './ExampleFormPage';

const ID = '00000000-0000-0000-0000-000000000001';

function renderForm(route: string) {
  renderWithProviders(
    <Routes>
      <Route path="/examples/new" element={<ExampleFormPage />} />
      <Route path="/examples/:id/edit" element={<ExampleFormPage />} />
      <Route path="/examples/:id" element={<p>Detail page</p>} />
    </Routes>,
    { route },
  );
}

describe('ExampleFormPage', () => {
  it('creates an example and goes to its detail page', async () => {
    let sent: unknown;
    server.use(
      getCreateExampleMockHandler(async ({ request }) => {
        sent = await request.json();
        return getCreateExampleResponseMock({ id: ID });
      }),
    );
    const user = userEvent.setup();
    renderForm('/examples/new');

    await user.type(screen.getByLabelText(/Title/), 'A new example');
    await user.selectOptions(screen.getByLabelText(/Status/), 'CLOSED');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Detail page')).toBeInTheDocument();
    expect(screen.getByText('Example saved.')).toBeInTheDocument();
    expect(sent).toEqual({ title: 'A new example', status: 'CLOSED', description: '' });
  });

  it('rejects a too long title before sending anything', async () => {
    // No handler for POST: if the form sent a request, the test would fail on it.
    const user = userEvent.setup();
    renderForm('/examples/new');

    await user.click(screen.getByLabelText(/Title/));
    await user.paste('x'.repeat(201));
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByLabelText(/Title/)).toHaveAttribute('aria-invalid', 'true');
  });

  it('shows the server field errors on the fields', async () => {
    server.use(
      http.post('*/api/examples', () =>
        HttpResponse.json(
          { status: 400, title: 'Bad Request', errors: [{ field: 'title', message: 'must not be blank' }] },
          { status: 400 },
        ),
      ),
    );
    const user = userEvent.setup();
    renderForm('/examples/new');

    await user.type(screen.getByLabelText(/Title/), '   ');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('must not be blank')).toBeInTheDocument();
    expect(screen.getByLabelText(/Title/)).toHaveAttribute('aria-invalid', 'true');
  });

  it('edits an existing example, sending the version it was based on', async () => {
    let sent: unknown;
    server.use(
      getGetExampleMockHandler(
        getGetExampleResponseMock({
          id: ID,
          title: 'Old title',
          status: 'OPEN',
          description: '',
          version: 7,
        }),
      ),
      getUpdateExampleMockHandler(async ({ request }) => {
        sent = await request.json();
        return getGetExampleResponseMock({ id: ID, title: 'New title' });
      }),
    );
    const user = userEvent.setup();
    renderForm(`/examples/${ID}/edit`);

    const title = await screen.findByLabelText(/Title/);
    expect(title).toHaveValue('Old title');
    await user.clear(title);
    await user.type(title, 'New title');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Detail page')).toBeInTheDocument();
    expect(sent).toEqual({ title: 'New title', status: 'OPEN', description: '', version: 7 });
  });

  it('says so when someone else saved the example in between', async () => {
    server.use(
      getGetExampleMockHandler(getGetExampleResponseMock({ id: ID, title: 'Old title', version: 7 })),
      http.put('*/api/examples/:id', () =>
        HttpResponse.json(
          { status: 409, title: 'Conflict', detail: 'Changed by someone else. Reload it and try again.' },
          { status: 409 },
        ),
      ),
    );
    const user = userEvent.setup();
    renderForm(`/examples/${ID}/edit`);

    await user.type(await screen.findByLabelText(/Title/), '!');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Changed by someone else. Reload it and try again.')).toBeInTheDocument();
    expect(screen.queryByText('Detail page')).not.toBeInTheDocument();
  });
});
