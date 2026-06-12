import TablePagination from '@mui/material/TablePagination'
import { USER_PAGE_SIZE_OPTIONS } from '../../types/users'

interface TablePaginationBarProps {
  page: number
  size: number
  totalElements: number
  onPageChange: (page: number) => void
  onSizeChange: (size: number) => void
}

export function TablePaginationBar({
  page,
  size,
  totalElements,
  onPageChange,
  onSizeChange,
}: TablePaginationBarProps) {
  return (
    <TablePagination
      component="div"
      count={totalElements}
      labelDisplayedRows={({ from, to, count }) => `${from}-${to} of ${count !== -1 ? count : `more than ${to}`}`}
      labelRowsPerPage="Rows per page"
      onPageChange={(_event, nextPage) => onPageChange(nextPage)}
      onRowsPerPageChange={(event) => onSizeChange(Number.parseInt(event.target.value, 10))}
      page={page}
      rowsPerPage={size}
      rowsPerPageOptions={[...USER_PAGE_SIZE_OPTIONS]}
    />
  )
}
