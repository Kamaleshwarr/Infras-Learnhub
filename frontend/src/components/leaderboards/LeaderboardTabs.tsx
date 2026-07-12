import { Box, Tab, Tabs } from '@mui/material'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import { LEADERBOARD_MESSAGES } from './leaderboardMessages'

export function LeaderboardTabs() {
  const location = useLocation()
  const initiativeTab = location.pathname.startsWith('/leaderboards/initiatives')

  return (
    <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
      <Tabs aria-label="Leaderboard views" value={initiativeTab ? 'initiative' : 'global'}>
        <Tab
          component={RouterLink}
          label={LEADERBOARD_MESSAGES.globalTab}
          to="/leaderboards/global"
          value="global"
        />
        <Tab
          component={RouterLink}
          label={LEADERBOARD_MESSAGES.initiativeTab}
          to="/leaderboards/initiatives"
          value="initiative"
        />
      </Tabs>
    </Box>
  )
}
