import { NavLink } from 'react-router-dom'
import {
  Box,
  Divider,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material'
import { navigationItems } from './navigation'
import { useAuth } from '../auth/useAuth'

export const drawerWidth = 280

interface SidebarProps {
  mobileOpen: boolean
  onClose: () => void
}

const navItemSx = {
  mx: 1.5,
  mb: 0.25,
  px: 1.5,
  py: 1,
  borderRadius: 2,
  color: 'text.primary',
  transition: 'background-color 0.15s ease, color 0.15s ease',
  '& .MuiListItemIcon-root': {
    minWidth: 40,
    color: 'text.secondary',
    transition: 'color 0.15s ease',
    '& .MuiSvgIcon-root': {
      fontSize: 22,
    },
  },
  '& .MuiListItemText-primary': {
    fontSize: '0.9375rem',
    fontWeight: 500,
    letterSpacing: '0.01em',
  },
  '&:hover': {
    bgcolor: 'action.hover',
    '& .MuiListItemIcon-root': {
      color: 'text.primary',
    },
  },
  '&.active': {
    bgcolor: 'primary.main',
    color: 'primary.contrastText',
    '& .MuiListItemIcon-root': {
      color: 'inherit',
    },
    '& .MuiListItemText-primary': {
      fontWeight: 600,
    },
    '&:hover': {
      bgcolor: 'primary.dark',
    },
  },
} as const

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  const { hasRole } = useAuth()
  const visibleItems = navigationItems.filter((item) => !item.roles || item.roles.some(hasRole))

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar sx={{ px: 2.5, minHeight: { xs: 64, sm: 72 } }}>
        <Box>
          <Typography sx={{ fontWeight: 700, letterSpacing: '-0.01em' }} variant="h6">
            Learning Hub
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.25 }} variant="body2">
            Internal enablement
          </Typography>
        </Box>
      </Toolbar>
      <Divider />
      <List
        aria-label="Main navigation"
        sx={{
          flex: 1,
          py: 1.5,
          px: 0,
          overflowY: 'auto',
        }}
      >
        {visibleItems.map((item) => (
          <ListItemButton
            component={NavLink}
            end={item.path === '/'}
            key={item.path}
            onClick={onClose}
            sx={navItemSx}
            to={item.path}
          >
            <ListItemIcon>
              <item.icon />
            </ListItemIcon>
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  )

  return (
    <>
      <Drawer
        ModalProps={{ keepMounted: true }}
        onClose={onClose}
        open={mobileOpen}
        sx={{ display: { xs: 'block', md: 'none' } }}
        variant="temporary"
      >
        <Box sx={{ width: drawerWidth }}>{drawerContent}</Box>
      </Drawer>
      <Drawer
        open
        sx={{
          display: { xs: 'none', md: 'block' },
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            borderRight: 1,
            borderColor: 'divider',
          },
        }}
        variant="permanent"
      >
        {drawerContent}
      </Drawer>
    </>
  )
}
