import MenuIcon from '@mui/icons-material/Menu'
import LogoutIcon from '@mui/icons-material/Logout'
import { AppBar, Box, IconButton, Toolbar, Typography, Button } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { NotificationBell } from '../components/notifications/NotificationBell'

interface HeaderProps {
  onMenuClick: () => void
}

export function Header({ onMenuClick }: HeaderProps) {
  const { currentRole, user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

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
            {user?.fullName ?? 'Authenticated user'}{currentRole ? ` · ${currentRole}` : ''}
          </Typography>
        </Box>
        <NotificationBell />
        <Button color="primary" onClick={handleLogout} startIcon={<LogoutIcon />} sx={{ ml: 1 }}>
          Logout
        </Button>
      </Toolbar>
    </AppBar>
  )
}
