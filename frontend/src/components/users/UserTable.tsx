import {
  Paper,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Typography,
} from '@mui/material'
import { SortableTableHead } from '../common/SortableTableHead'
import type { SortableColumn } from '../common/SortableTableHead'
import type { UserSummary } from '../../types/users'
import { UserRoleChip } from './UserRoleChip'
import { UserStatusChip } from './UserStatusChip'

interface UserTableProps {
  users: UserSummary[]
  sort: string
  loading: boolean
  showMustChangePasswordColumn: boolean
  hasActiveFilters: boolean
  onSort: (property: string) => void
}

const BASE_COLUMNS: SortableColumn[] = [
  { id: 'employeeId', label: 'Employee ID', sortable: true },
  { id: 'fullName', label: 'Full Name', sortable: true },
  { id: 'email', label: 'Email', sortable: true },
  { id: 'role', label: 'Role' },
  { id: 'active', label: 'Status', sortable: true },
]

const MUST_CHANGE_PASSWORD_COLUMN: SortableColumn = {
  id: 'mustChangePassword',
  label: 'Must Change Password',
}

export function UserTable({
  users,
  sort,
  loading,
  showMustChangePasswordColumn,
  hasActiveFilters,
  onSort,
}: UserTableProps) {
  const columns = showMustChangePasswordColumn
    ? [...BASE_COLUMNS, MUST_CHANGE_PASSWORD_COLUMN]
    : BASE_COLUMNS

  if (loading) {
    return (
      <Paper variant="outlined">
        <TableContainer>
          <Table>
            <SortableTableHead columns={columns} onSort={onSort} sort={sort} />
            <TableBody>
              {Array.from({ length: 5 }).map((_, index) => (
                <TableRow key={index}>
                  {columns.map((column) => (
                    <TableCell key={column.id}>
                      <Skeleton />
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    )
  }

  if (users.length === 0) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }} variant="outlined">
        <Typography color="text.secondary">
          {hasActiveFilters ? 'No users match your filters.' : 'No users found yet.'}
        </Typography>
      </Paper>
    )
  }

  return (
    <Paper variant="outlined">
      <TableContainer>
        <Table>
          <SortableTableHead columns={columns} onSort={onSort} sort={sort} />
          <TableBody>
            {users.map((user) => (
              <TableRow hover key={user.id}>
                <TableCell>{user.employeeId}</TableCell>
                <TableCell>{user.fullName}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>
                  <UserRoleChip role={user.role} />
                </TableCell>
                <TableCell>
                  <UserStatusChip active={user.active} />
                </TableCell>
                {showMustChangePasswordColumn ? (
                  <TableCell>{user.mustChangePassword ? 'Yes' : 'No'}</TableCell>
                ) : null}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  )
}
