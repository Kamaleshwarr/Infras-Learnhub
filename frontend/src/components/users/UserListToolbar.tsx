import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import { Box, Button } from '@mui/material'

interface UserListToolbarProps {
  onCreateUser: () => void
}

export function UserListToolbar({ onCreateUser }: UserListToolbarProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
      <Button onClick={onCreateUser} startIcon={<AddOutlinedIcon />} variant="contained">
        Create User
      </Button>
    </Box>
  )
}
