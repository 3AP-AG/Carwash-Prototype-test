// ui/select: a choice from a fixed list, as a native <select> so it works with register() and
// with assistive technology. Pass {...register('name')} from React Hook Form.
import { FormField, type FieldProps } from './form-field';

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps extends FieldProps {
  options: SelectOption[];
}

export function Select({ options, ...props }: SelectProps) {
  return (
    <FormField {...props}>
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </FormField>
  );
}
