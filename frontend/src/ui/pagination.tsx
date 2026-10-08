// ui/pagination: page controls under a list. Pages are zero-based, like PageResponse.
import TablePagination from '@mui/material/TablePagination';

export interface PaginationProps {
  page: number;
  size: number;
  totalItems: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ page, size, totalItems, onPageChange }: PaginationProps) {
  return (
    <TablePagination
      component="div"
      count={totalItems}
      page={page}
      rowsPerPage={size}
      rowsPerPageOptions={[]}
      onPageChange={(_, next) => onPageChange(next)}
    />
  );
}
