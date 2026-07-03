import MenuBookOutlinedIcon from '@mui/icons-material/MenuBookOutlined'
import HomeOutlinedIcon from '@mui/icons-material/HomeOutlined'
import SettingsOutlinedIcon from '@mui/icons-material/SettingsOutlined'
import { Box, Tab, Tabs } from '@mui/material'
import { Link as RouterLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { LEARN_MESSAGES } from '../components/learn/learnMessages'

function learnTabValue(pathname: string) {
  if (pathname.startsWith('/learn/manage')) {
    return '/learn/manage'
  }
  if (pathname.startsWith('/learn/technologies')) {
    return '/learn/technologies'
  }
  return '/learn'
}

export function LearnLayout() {
  const location = useLocation()
  const { isAdmin } = useAuth()
  const currentTab = learnTabValue(location.pathname)

  return (
    <Box>
      <Tabs aria-label="Learn navigation" sx={{ mb: 3 }} value={currentTab}>
        <Tab
          component={RouterLink}
          icon={<HomeOutlinedIcon />}
          iconPosition="start"
          label="Home"
          to="/learn"
          value="/learn"
        />
        <Tab
          component={RouterLink}
          icon={<MenuBookOutlinedIcon />}
          iconPosition="start"
          label="Technologies"
          to="/learn/technologies"
          value="/learn/technologies"
        />
        {isAdmin ? (
          <Tab
            component={RouterLink}
            icon={<SettingsOutlinedIcon />}
            iconPosition="start"
            label="Manage"
            to="/learn/manage"
            value="/learn/manage"
          />
        ) : null}
      </Tabs>
      <Outlet />
    </Box>
  )
}

export function LearnPageIntro() {
  return (
    <Box sx={{ color: 'text.secondary', mb: 3 }}>
      <Box component="p" sx={{ m: 0 }}>
        {LEARN_MESSAGES.whereAmI}
      </Box>
      <Box component="p" sx={{ m: 0 }}>
        {LEARN_MESSAGES.whatIsThis}
      </Box>
    </Box>
  )
}
