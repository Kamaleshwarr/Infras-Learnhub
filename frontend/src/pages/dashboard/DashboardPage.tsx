import { useEffect, useMemo, useState } from 'react'
import AssignmentTurnedInOutlinedIcon from '@mui/icons-material/AssignmentTurnedInOutlined'
import AutoStoriesOutlinedIcon from '@mui/icons-material/AutoStoriesOutlined'
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import LibraryBooksOutlinedIcon from '@mui/icons-material/LibraryBooksOutlined'
import PendingActionsOutlinedIcon from '@mui/icons-material/PendingActionsOutlined'
import PeopleOutlinedIcon from '@mui/icons-material/PeopleOutlined'
import SchoolOutlinedIcon from '@mui/icons-material/SchoolOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import WorkspacePremiumOutlinedIcon from '@mui/icons-material/WorkspacePremiumOutlined'
import { Alert, Box, Button, Chip, Grid, Stack } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { getAdminDashboardData, getEmployeeDashboardData } from '../../api/dashboardApi'
import type { DashboardData } from '../../api/dashboardApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { DashboardListCard } from '../../components/dashboard/DashboardListCard'
import type { DashboardListItem } from '../../components/dashboard/DashboardListCard'
import {
  DashboardActivityCard,
  DashboardNotificationsCard,
  DashboardQuickActionsCard,
  DashboardRankCard,
  type DashboardQuickAction,
} from '../../components/dashboard/DashboardPanels'
import { DashboardSectionHeader, DashboardWelcomeBanner } from '../../components/dashboard/DashboardSection'
import { DashboardWidget } from '../../components/dashboard/DashboardWidget'
import { SubmissionStatusChip } from '../../components/submissions/SubmissionStatusChip'

export function DashboardPage() {
  const { isAdmin, user } = useAuth()
  const [data, setData] = useState<DashboardData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true

    async function loadDashboard() {
      setLoading(true)
      setError(null)
      try {
        const dashboardData = isAdmin ? await getAdminDashboardData() : await getEmployeeDashboardData()
        if (mounted) {
          setData(dashboardData)
        }
      } catch {
        if (mounted) {
          setError('Unable to load dashboard data. Please refresh or try again later.')
          setData(null)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    loadDashboard()
    return () => {
      mounted = false
    }
  }, [isAdmin])

  const greeting = useMemo(() => getGreeting(user?.fullName), [user?.fullName])
  const metricCards = isAdmin ? adminMetrics(data) : employeeMetrics(data)
  const quickActions = isAdmin ? adminQuickActions() : employeeQuickActions()

  return (
    <Box>
      <PageHeader
        description={
          isAdmin
            ? 'Executive overview of learning operations, certifications, and engagement.'
            : 'Track your learning progress, certifications, and team standing.'
        }
        title="Dashboard"
      />

      {error ? (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      ) : null}

      <DashboardWelcomeBanner
        actions={
          isAdmin ? (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              <Button component={RouterLink} to="/submissions/review" variant="contained">
                Review submissions
              </Button>
              <Button component={RouterLink} to="/initiatives" variant="outlined">
                Manage initiatives
              </Button>
            </Stack>
          ) : (
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
              <Button component={RouterLink} to="/submissions/new" variant="contained">
                Submit certificate
              </Button>
              <Button component={RouterLink} to="/learn" variant="outlined">
                Explore Learn
              </Button>
            </Stack>
          )
        }
        greeting={greeting}
        subtitle={
          isAdmin
            ? 'Monitor platform health, pending reviews, and learner engagement at a glance.'
            : 'Your personalized hub for initiatives, certifications, and learning momentum.'
        }
      />

      <DashboardSectionHeader
        description={isAdmin ? 'Key operational metrics across the platform.' : 'A snapshot of your current learning status.'}
        title="Quick Statistics"
      />
      <Grid container spacing={2.5} sx={{ mb: 4 }}>
        {metricCards.map((widget) => (
          <Grid key={widget.title} size={{ xs: 12, sm: 6, lg: 3 }}>
            <DashboardWidget loading={loading} {...widget} />
          </Grid>
        ))}
      </Grid>

      {isAdmin ? (
        <AdminDashboardContent data={data} loading={loading} quickActions={quickActions} />
      ) : (
        <EmployeeDashboardContent data={data} loading={loading} quickActions={quickActions} />
      )}
    </Box>
  )
}

function AdminDashboardContent({
  data,
  loading,
  quickActions,
}: {
  data: DashboardData | null
  loading: boolean
  quickActions: DashboardQuickAction[]
}) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, lg: 8 }}>
        <Stack spacing={2.5}>
          <DashboardListCard
            description="Programs currently open to employees."
            emptyText="No active initiatives."
            items={initiativeItems(data)}
            loading={loading}
            title="Active Initiatives"
            viewAllHref="/initiatives"
          />
          <DashboardListCard
            description="Top performers by approved certification count."
            emptyText="No leaderboard data yet."
            items={leaderboardItems(data)}
            loading={loading}
            title="Leaderboard Snapshot"
            viewAllHref="/leaderboards/global"
          />
          <DashboardActivityCard
            emptyText="No recent platform activity."
            items={data?.recentActivity ?? []}
            loading={loading}
          />
        </Stack>
      </Grid>
      <Grid size={{ xs: 12, lg: 4 }}>
        <Stack spacing={2.5}>
          <DashboardNotificationsCard
            emptyText="You're all caught up."
            loading={loading}
            notifications={data?.recentNotifications ?? []}
            unreadCount={data?.unreadNotificationsCount ?? 0}
          />
          <DashboardQuickActionsCard actions={quickActions} loading={loading} />
          {data?.expiringInitiativesCount ? (
            <Alert severity="warning">
              {data.expiringInitiativesCount} initiative{data.expiringInitiativesCount === 1 ? '' : 's'} expiring within 14 days.
            </Alert>
          ) : null}
        </Stack>
      </Grid>
    </Grid>
  )
}

function EmployeeDashboardContent({
  data,
  loading,
  quickActions,
}: {
  data: DashboardData | null
  loading: boolean
  quickActions: DashboardQuickAction[]
}) {
  return (
    <Grid container spacing={2.5}>
      <Grid size={{ xs: 12, lg: 8 }}>
        <Stack spacing={2.5}>
          <DashboardListCard
            description="Learning programs you can participate in now."
            emptyText="No active initiatives available."
            items={initiativeItems(data, true)}
            loading={loading}
            title="My Active Initiatives"
            viewAllHref="/initiatives"
          />
          <DashboardListCard
            description="Your latest certificate submissions."
            emptyText="No certificates submitted yet."
            items={certificateItems(data)}
            loading={loading}
            title="My Certificates"
            viewAllHref="/submissions"
          />
          <DashboardActivityCard
            emptyText="No recent activity yet. Submit a certificate or explore initiatives to get started."
            items={data?.recentActivity ?? []}
            loading={loading}
          />
        </Stack>
      </Grid>
      <Grid size={{ xs: 12, lg: 4 }}>
        <Stack spacing={2.5}>
          <DashboardRankCard
            loading={loading}
            rank={data?.myRank?.globalRank ?? null}
            totalApprovedCertifications={data?.myRank?.totalApprovedCertifications ?? 0}
          />
          <DashboardNotificationsCard
            emptyText="No notifications yet."
            loading={loading}
            notifications={data?.recentNotifications ?? []}
            unreadCount={data?.unreadNotificationsCount ?? 0}
          />
          <DashboardQuickActionsCard actions={quickActions} loading={loading} />
        </Stack>
      </Grid>
    </Grid>
  )
}

function adminMetrics(data: DashboardData | null) {
  return [
    {
      accentColor: 'info' as const,
      helperText: 'Registered platform users',
      href: '/users',
      icon: <PeopleOutlinedIcon />,
      linkAriaLabel: 'View all users',
      title: 'Total Users',
      value: formatNumber(data?.totalUsersCount),
    },
    {
      accentColor: 'primary' as const,
      helperText: 'Currently active learning programs',
      href: '/initiatives',
      icon: <SchoolOutlinedIcon />,
      linkAriaLabel: 'View active initiatives',
      title: 'Active Initiatives',
      value: formatNumber(data?.activeInitiativesCount),
    },
    {
      accentColor: 'warning' as const,
      helperText: 'Certificate submissions awaiting review',
      href: '/submissions/review',
      icon: <PendingActionsOutlinedIcon />,
      linkAriaLabel: `View ${formatNumber(data?.pendingReviewsCount)} pending certificate reviews`,
      title: 'Pending Reviews',
      value: formatNumber(data?.pendingReviewsCount),
    },
    {
      accentColor: 'success' as const,
      helperText: 'All certificate submissions on the platform',
      href: '/submissions/review',
      icon: <AssignmentTurnedInOutlinedIcon />,
      linkAriaLabel: 'View certificate submissions',
      title: 'Certificates Submitted',
      value: formatNumber(data?.certificatesSubmittedCount),
    },
  ]
}

function employeeMetrics(data: DashboardData | null) {
  return [
    {
      accentColor: 'primary' as const,
      helperText: 'Learning programs available now',
      href: '/initiatives',
      icon: <SchoolOutlinedIcon />,
      linkAriaLabel: 'Browse initiatives',
      title: 'Active Initiatives',
      value: formatNumber(data?.activeInitiativesCount),
    },
    {
      accentColor: 'info' as const,
      helperText: 'Total certificate submissions',
      href: '/submissions',
      icon: <LibraryBooksOutlinedIcon />,
      linkAriaLabel: 'View my certifications',
      title: 'My Certificates',
      value: formatNumber(data?.mySubmissionsTotalCount),
    },
    {
      accentColor: 'secondary' as const,
      helperText: 'Your current global rank',
      href: '/leaderboards/global',
      icon: <WorkspacePremiumOutlinedIcon />,
      linkAriaLabel: 'View global leaderboard',
      title: 'Leaderboard Rank',
      value: data?.myRank?.globalRank ? `#${data.myRank.globalRank}` : '--',
    },
    {
      accentColor: 'success' as const,
      helperText: 'Approved certifications',
      href: '/submissions',
      icon: <EmojiEventsOutlinedIcon />,
      linkAriaLabel: 'View approved certifications',
      title: 'Approved Certifications',
      value: formatNumber(data?.myRank?.totalApprovedCertifications),
    },
  ]
}

function adminQuickActions(): DashboardQuickAction[] {
  return [
    {
      id: 'review',
      label: 'Review Submissions',
      description: 'Approve or reject certificates',
      href: '/submissions/review',
      icon: PendingActionsOutlinedIcon,
    },
    {
      id: 'initiatives',
      label: 'Manage Initiatives',
      description: 'Create and publish programs',
      href: '/initiatives',
      icon: SchoolOutlinedIcon,
    },
    {
      id: 'users',
      label: 'Manage Users',
      description: 'Accounts and access control',
      href: '/users',
      icon: PeopleOutlinedIcon,
    },
    {
      id: 'leaderboards',
      label: 'View Leaderboards',
      description: 'Global and initiative rankings',
      href: '/leaderboards/global',
      icon: EmojiEventsOutlinedIcon,
    },
    {
      id: 'learn-manage',
      label: 'Learn Catalog',
      description: 'Curate technologies and resources',
      href: '/learn/manage',
      icon: AutoStoriesOutlinedIcon,
    },
  ]
}

function employeeQuickActions(): DashboardQuickAction[] {
  return [
    {
      id: 'submit',
      label: 'Submit Certificate',
      description: 'Upload proof of completion',
      href: '/submissions/new',
      icon: UploadFileOutlinedIcon,
    },
    {
      id: 'initiatives',
      label: 'Browse Initiatives',
      description: 'Explore learning programs',
      href: '/initiatives',
      icon: SchoolOutlinedIcon,
    },
    {
      id: 'learn',
      label: 'Explore Learn',
      description: 'Technologies and roadmaps',
      href: '/learn',
      icon: AutoStoriesOutlinedIcon,
    },
    {
      id: 'leaderboards',
      label: 'View Leaderboards',
      description: 'See how you compare',
      href: '/leaderboards/global',
      icon: EmojiEventsOutlinedIcon,
    },
    {
      id: 'certifications',
      label: 'My Certifications',
      description: 'Track submission status',
      href: '/submissions',
      icon: LibraryBooksOutlinedIcon,
    },
  ]
}

function initiativeItems(data: DashboardData | null, linkToDetail = false): DashboardListItem[] {
  return (data?.activeInitiatives ?? []).map((initiative) => ({
    href: linkToDetail ? `/initiatives/${initiative.id}` : '/initiatives',
    id: initiative.id,
    primary: initiative.title,
    secondary: `Expires ${formatDate(initiative.expiryDateUtc)}`,
  }))
}

function certificateItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.mySubmissions ?? []).map((submission) => ({
    href: '/submissions',
    id: submission.id,
    primary: (
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Box component="span" sx={{ minWidth: 0 }}>
          {submission.initiative.title}
        </Box>
        <SubmissionStatusChip status={submission.approvalStatus} />
      </Stack>
    ),
    secondary: `Submitted ${formatDate(submission.submittedAtUtc)}`,
  }))
}

function leaderboardItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.leaderboardPreview ?? []).map((entry) => ({
    href: '/leaderboards/global',
    id: entry.employee.id,
    primary: (
      <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
        <Chip color="primary" label={`#${entry.rank}`} size="small" variant="outlined" />
        <Box component="span">{entry.employee.fullName}</Box>
      </Stack>
    ),
    secondary: entry.totalApprovedCertifications
      ? `${entry.totalApprovedCertifications} approved certification${entry.totalApprovedCertifications === 1 ? '' : 's'}`
      : entry.employee.email,
  }))
}

function getGreeting(fullName?: string) {
  const hour = new Date().getHours()
  const salutation = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening'
  return fullName ? `${salutation}, ${fullName.split(' ')[0]}` : salutation
}

function formatNumber(value: number | undefined) {
  return value == null ? '--' : String(value)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
