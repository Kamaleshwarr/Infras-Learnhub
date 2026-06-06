import { Box } from '@mui/material'
import { DashboardWidget } from '../../components/dashboard/DashboardWidget'
import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

const dashboardWidgets = [
  { title: 'Active Initiatives', value: '--', helperText: 'Connected to /initiatives in the next build step' },
  { title: 'My Submissions', value: '--', helperText: 'Connected to /me/submissions in the next build step' },
  { title: 'Leaderboard Preview', value: '--', helperText: 'Connected to /leaderboards/global' },
  { title: 'Recent Materials', value: '--', helperText: 'Connected to study materials search' },
]

export function DashboardPage() {
  return (
    <Box>
      <PageHeader
        description="Overview of initiatives, submissions, leaderboards, and learning resources."
        title="Dashboard"
      />
      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' },
          mb: 3,
        }}
      >
        {dashboardWidgets.map((widget) => (
          <DashboardWidget key={widget.title} {...widget} />
        ))}
      </Box>
      <PlaceholderPanel
        items={[
          'Active Initiatives',
          'My Submissions',
          'Leaderboard Preview',
          'Recently uploaded study materials',
        ]}
        title="Dashboard widgets planned for Frontend Phase 1 implementation"
      />
    </Box>
  )
}
