// The ui/ components the Example pages don't cover. Queried by role and text, like every test here.
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithProviders } from '@/test/setup';
import { ConfirmDialog, Pagination, useToast } from '@/ui';

describe('ConfirmDialog', () => {
  it('confirms or cancels', async () => {
    const onConfirm = vi.fn();
    const onCancel = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(
      <ConfirmDialog
        open
        title="Delete it?"
        message="This cannot be undone."
        confirmLabel="Delete"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByRole('dialog', { name: 'Delete it?' })).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Delete' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(onConfirm).toHaveBeenCalledOnce();
    expect(onCancel).toHaveBeenCalledOnce();
  });
});

describe('Pagination', () => {
  it('moves to the next page', async () => {
    const onPageChange = vi.fn();
    const user = userEvent.setup();
    renderWithProviders(<Pagination page={0} size={20} totalItems={41} onPageChange={onPageChange} />);

    await user.click(screen.getByRole('button', { name: /next page/i }));

    expect(onPageChange).toHaveBeenCalledWith(1);
  });
});

describe('useToast', () => {
  function SaveButton() {
    const toast = useToast();
    return <button onClick={() => toast.show('Saved.')}>Save</button>;
  }

  it('shows the message', async () => {
    const user = userEvent.setup();
    renderWithProviders(<SaveButton />);

    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(await screen.findByText('Saved.')).toBeInTheDocument();
  });
});
