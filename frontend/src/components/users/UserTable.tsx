import BlockOutlinedIcon from '@mui/icons-material/BlockOutlined'
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LockResetOutlinedIcon from '@mui/icons-material/LockResetOutlined'
import {
  IconButton,
  Paper,
  Skeleton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Tooltip,
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
  currentUserId?: string
  onSort: (property: string) => void
  onEdit: (user: UserSummary) => void
  onActivate: (user: UserSummary) => void
  onDeactivate: (user: UserSummary) => void
  onResetPassword: (user: UserSummary) => void
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

const ACTIONS_COLUMN: SortableColumn = {
  id: 'actions',
  label: 'Actions',
  align: 'right',
}

export function UserTable({
  users,
  sort,
  loading,
  showMustChangePasswordColumn,
  hasActiveFilters,
  currentUserId,
  onSort,
  onEdit,
  onActivate,
  onDeactivate,
  onResetPassword,
}: UserTableProps) {
  const columns = [
    ...BASE_COLUMNS,
    ...(showMustChangePasswordColumn ? [MUST_CHANGE_PASSWORD_COLUMN] : []),
    ACTIONS_COLUMN,
  ]

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
            {users.map((user) => {
              const isSelf = Boolean(currentUserId && user.id === currentUserId)

              return (
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
                  <TableCell align="right">
                    <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center', justifyContent: 'flex-end' }}>
                      {user.active ? (
                        <Tooltip
                          title={
                            isSelf ? 'You cannot deactivate your own account.' : 'Deactivate user'
                          }
                        >
                          <span>
                            <IconButton
                              aria-disabled={isSelf}
                              aria-label={`Deactivate user ${user.fullName}`}
                              disabled={isSelf}
                              onClick={isSelf ? undefined : () => onDeactivate(user)}
                              size="small"
                            >
                              <BlockOutlinedIcon fontSize="small" />
                            </IconButton>
                          </span>
                        </Tooltip>
                      ) : (
                        <Tooltip title="Activate user">
                          <IconButton
                            aria-label={`Activate user ${user.fullName}`}
                            onClick={() => onActivate(user)}
                            size="small"
                          >
                              <CheckCircleOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                      <Tooltip title="Reset password">
                        <IconButton
                          aria-label={`Reset password for ${user.fullName}`}
                          onClick={() => onResetPassword(user)}
                          size="small"
                        >
                          <LockResetOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Edit user">
                        <IconButton
                          aria-label={`Edit user ${user.fullName}`}
                          onClick={() => onEdit(user)}
                          size="small"
                        >
                          <EditOutlinedIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Stack>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  )
}
