import TableCell from '@mui/material/TableCell'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import TableSortLabel from '@mui/material/TableSortLabel'

export interface SortableColumn {
  id: string
  label: string
  sortable?: boolean
  align?: 'left' | 'right' | 'center'
}

interface SortableTableHeadProps {
  columns: SortableColumn[]
  sort: string
  onSort: (property: string) => void
}

export function SortableTableHead({ columns, sort, onSort }: SortableTableHeadProps) {
  const [activeProperty, direction] = sort.split(',')
  const activeDirection = direction?.toLowerCase() === 'desc' ? 'desc' : 'asc'

  return (
    <TableHead>
      <TableRow>
        {columns.map((column) => (
          <TableCell align={column.align ?? 'left'} key={column.id}>
            {column.sortable ? (
              <TableSortLabel
                active={activeProperty === column.id}
                direction={activeProperty === column.id ? activeDirection : 'asc'}
                onClick={() => onSort(column.id)}
              >
                {column.label}
              </TableSortLabel>
            ) : (
              column.label
            )}
          </TableCell>
        ))}
      </TableRow>
    </TableHead>
  )
}
