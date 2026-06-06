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

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  const { hasRole } = useAuth()
  const visibleItems = navigationItems.filter((item) => !item.roles || item.roles.some(hasRole))

  const drawerContent = (
    <Box>
      <Toolbar>
        <Box>
          <Typography variant="h6">Learning Hub</Typography>
          <Typography color="text.secondary" variant="body2">
            Internal enablement
          </Typography>
        </Box>
      </Toolbar>
      <Divider />
      <List>
        {visibleItems.map((item) => (
          <ListItemButton
            component={NavLink}
            key={item.path}
            onClick={onClose}
            sx={{
              mx: 1,
              my: 0.5,
              borderRadius: 2,
              '&.active': {
                bgcolor: 'primary.main',
                color: 'primary.contrastText',
                '& .MuiListItemIcon-root': {
                  color: 'inherit',
                },
              },
            }}
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
          '& .MuiDrawer-paper': {
            width: drawerWidth,
          },
        }}
        variant="permanent"
      >
        {drawerContent}
      </Drawer>
    </>
  )
}
