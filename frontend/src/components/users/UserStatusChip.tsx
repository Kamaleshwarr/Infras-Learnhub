import Chip from '@mui/material/Chip'

interface UserStatusChipProps {
  active: boolean
}

export function UserStatusChip({ active }: UserStatusChipProps) {
  return (
    <Chip
      color={active ? 'success' : 'default'}
      label={active ? 'Active' : 'Inactive'}
      size="small"
      variant={active ? 'filled' : 'outlined'}
    />
  )
}
