import Chip from '@mui/material/Chip'
import type { UserRole } from '../../types/auth'

interface UserRoleChipProps {
  role: UserRole
}

export function UserRoleChip({ role }: UserRoleChipProps) {
  return <Chip color={role === 'ADMIN' ? 'primary' : 'default'} label={role} size="small" variant="outlined" />
}
