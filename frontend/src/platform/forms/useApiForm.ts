// React Hook Form wired to a generated Zod schema and to the server's ProblemDetail errors.
// Client: the schema from src/api/generated/<tag>/<tag>.zod.ts validates before sending.
// Server: errors[{field, message}] land on the matching fields; anything else (a 409, a 500, a field
// the form doesn't have) becomes the form-level error, `formError`.
import { zodResolver } from '@hookform/resolvers/zod';
import {
  useForm,
  type DefaultValues,
  type FieldValues,
  type Path,
  type UseFormReturn,
} from 'react-hook-form';
import type { z } from 'zod';
import { ApiError } from '@/api/client';

export interface ApiForm<T extends FieldValues> extends UseFormReturn<T> {
  /** Pass to a mutation's onError. */
  handleServerError: (error: unknown) => void;
  /** The form-level error, for <Form error={…}>. */
  formError: string | undefined;
}

export function useApiForm<T extends FieldValues>(
  schema: z.ZodType<T, T>,
  defaultValues: DefaultValues<T>,
): ApiForm<T> {
  const form = useForm<T>({ resolver: zodResolver(schema), defaultValues, mode: 'onTouched' });
  const fields = new Set(Object.keys(defaultValues));

  const handleServerError = (error: unknown) => {
    const problem = error instanceof ApiError ? error.problem : undefined;
    const unmatched: string[] = [];
    for (const { field, message } of problem?.errors ?? []) {
      if (fields.has(field)) {
        form.setError(field as Path<T>, { type: 'server', message });
      } else {
        unmatched.push(message);
      }
    }
    if (!problem?.errors?.length || unmatched.length > 0) {
      const message =
        unmatched.length > 0 ? unmatched.join(' ') : (problem?.detail ?? 'Something went wrong.');
      form.setError('root.server', { type: 'server', message });
    }
  };

  return { ...form, handleServerError, formError: form.formState.errors.root?.server?.message };
}
