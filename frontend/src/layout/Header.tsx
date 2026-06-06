import MenuIcon from '@mui/icons-material/Menu'
import LogoutIcon from '@mui/icons-material/Logout'
import { AppBar, Box, IconButton, Toolbar, Typography, Button } from '@mui/material'
import { useAuth } from '../auth/useAuth'

interface HeaderProps {
  onMenuClick: () => void
}

export function Header({ onMenuClick }: HeaderProps) {
  const { user, logout } = useAuth()

  return (
    <AppBar
      color="inherit"
      elevation={0}
      position="sticky"
      sx={{ borderBottom: 1, borderColor: 'divider' }}
    >
      <Toolbar>
        <IconButton
          aria-label="Open navigation"
          edge="start"
          onClick={onMenuClick}
          sx={{ display: { md: 'none' }, mr: 1 }}
        >
          <MenuIcon />
        </IconButton>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="h6">Engineering Learning Hub</Typography>
          <Typography color="text.secondary" variant="body2">
            {user?.fullName ?? 'Authenticated user'}
          </Typography>
        </Box>
        <Button color="primary" onClick={logout} startIcon={<LogoutIcon />}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  )
}
