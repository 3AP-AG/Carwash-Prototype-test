// ui/input: a single-line text field. Pass {...register('name')} from React Hook Form.
import { FormField, type FieldProps } from './form-field';

export interface InputProps extends FieldProps {
  type?: 'text' | 'email' | 'number' | 'password' | 'search';
}

export function Input(props: InputProps) {
  return <FormField {...props} />;
}
