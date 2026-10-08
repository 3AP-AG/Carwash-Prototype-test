// ui/textarea: a multi-line text field. Pass {...register('name')} from React Hook Form.
import { FormField, type FieldProps } from './form-field';

export interface TextareaProps extends FieldProps {
  minRows?: number;
}

export function Textarea({ minRows = 3, ...props }: TextareaProps) {
  return <FormField {...props} multiline minRows={minRows} />;
}
