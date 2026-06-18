import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import AssignmentTurnedInOutlinedIcon from '@mui/icons-material/AssignmentTurnedInOutlined'
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import PendingActionsOutlinedIcon from '@mui/icons-material/PendingActionsOutlined'
import SchoolOutlinedIcon from '@mui/icons-material/SchoolOutlined'
import TimelapseOutlinedIcon from '@mui/icons-material/TimelapseOutlined'
import WorkspacePremiumOutlinedIcon from '@mui/icons-material/WorkspacePremiumOutlined'
import { Alert, Box } from '@mui/material'
import { getAdminDashboardData, getEmployeeDashboardData } from '../../api/dashboardApi'
import type { DashboardData } from '../../api/dashboardApi'
import { useAuth } from '../../auth/useAuth'
import { DashboardWidget } from '../../components/dashboard/DashboardWidget'
import { DashboardListCard } from '../../components/dashboard/DashboardListCard'
import type { DashboardListItem } from '../../components/dashboard/DashboardListCard'
import { PageHeader } from '../../components/common/PageHeader'

export function DashboardPage() {
  const { isAdmin } = useAuth()
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

  const metricCards = isAdmin ? adminMetrics(data) : employeeMetrics(data)

  return (
    <Box>
      <PageHeader
        description={isAdmin ? 'Operational overview for learning administrators.' : 'Your learning activity and resources.'}
        title={isAdmin ? 'Admin Dashboard' : 'Employee Dashboard'}
      />
      {error ? (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      ) : null}
      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', lg: 'repeat(4, 1fr)' },
          mb: 3,
        }}
      >
        {metricCards.map((widget) => (
          <DashboardWidget key={widget.title} loading={loading} {...widget} />
        ))}
      </Box>
      {isAdmin ? <AdminDashboardLists data={data} loading={loading} /> : <EmployeeDashboardLists data={data} loading={loading} />}
    </Box>
  )
}

function adminMetrics(data: DashboardData | null) {
  return [
    {
      helperText: 'Currently active learning programs',
      icon: <SchoolOutlinedIcon />,
      title: 'Active Initiatives',
      value: formatNumber(data?.activeInitiativesCount),
    },
    {
      helperText: 'Active initiatives expiring in 14 days',
      icon: <TimelapseOutlinedIcon />,
      title: 'Expiring Initiatives',
      value: formatNumber(data?.expiringInitiativesCount),
    },
    {
      helperText: 'Certificate submissions awaiting review',
      href: '/submissions/review',
      icon: <PendingActionsOutlinedIcon />,
      linkAriaLabel: `View ${formatNumber(data?.pendingReviewsCount)} pending certificate reviews`,
      title: 'Pending Reviews',
      value: formatNumber(data?.pendingReviewsCount),
    },
    {
      helperText: 'Top learners shown below',
      icon: <EmojiEventsOutlinedIcon />,
      title: 'Top Learners',
      value: formatNumber(data?.leaderboardPreview.length),
    },
  ]
}

function employeeMetrics(data: DashboardData | null) {
  return [
    {
      helperText: 'Learning programs available now',
      icon: <SchoolOutlinedIcon />,
      title: 'Active Initiatives',
      value: formatNumber(data?.activeInitiativesCount),
    },
    {
      helperText: 'Recent certificate submissions',
      icon: <AssignmentTurnedInOutlinedIcon />,
      title: 'My Submissions',
      value: formatNumber(data?.mySubmissions.length),
    },
    {
      helperText: 'Your current global rank',
      icon: <WorkspacePremiumOutlinedIcon />,
      title: 'My Rank',
      value: data?.myRank?.globalRank ? `#${data.myRank.globalRank}` : '--',
    },
    {
      helperText: 'Approved certifications',
      icon: <EmojiEventsOutlinedIcon />,
      title: 'Approved Certifications',
      value: formatNumber(data?.myRank?.totalApprovedCertifications),
    },
  ]
}

function AdminDashboardLists({ data, loading }: { data: DashboardData | null; loading: boolean }) {
  return (
    <DashboardGrid>
      <DashboardListCard emptyText="No leaderboard data yet." items={leaderboardItems(data)} loading={loading} title="Top Learners Preview" />
      <DashboardListCard emptyText="No recent study materials." items={materialItems(data)} loading={loading} title="Recent Study Materials" />
      <DashboardListCard emptyText="No recent project updates." items={projectItems(data?.recentProjectUpdates)} loading={loading} title="Recent Project Updates" />
    </DashboardGrid>
  )
}

function EmployeeDashboardLists({ data, loading }: { data: DashboardData | null; loading: boolean }) {
  return (
    <DashboardGrid>
      <DashboardListCard emptyText="No active initiatives available." items={initiativeItems(data)} loading={loading} title="Active Initiatives" />
      <DashboardListCard emptyText="No submissions yet." items={submissionItems(data)} loading={loading} title="My Submissions" />
      <DashboardListCard emptyText="No leaderboard data yet." items={leaderboardItems(data)} loading={loading} title="Leaderboard Preview" />
      <DashboardListCard emptyText="No recent study materials." items={materialItems(data)} loading={loading} title="Recent Study Materials" />
      <DashboardListCard emptyText="No assigned projects." items={projectItems(data?.assignedProjects)} loading={loading} title="Assigned Projects" />
    </DashboardGrid>
  )
}

function DashboardGrid({ children }: { children: ReactNode }) {
  return (
    <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: 'repeat(2, 1fr)' } }}>
      {children}
    </Box>
  )
}

function initiativeItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.activeInitiatives ?? []).map((initiative) => ({
    id: initiative.id,
    primary: initiative.title,
    secondary: `Expires ${formatDate(initiative.expiryDateUtc)}`,
  }))
}

function submissionItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.mySubmissions ?? []).map((submission) => ({
    id: submission.id,
    primary: submission.approvalStatus,
    secondary: `Submitted ${formatDate(submission.submittedAtUtc)}`,
  }))
}

function leaderboardItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.leaderboardPreview ?? []).map((entry) => ({
    id: entry.employee.id,
    primary: `#${entry.rank} ${entry.employee.fullName}`,
    secondary: entry.totalApprovedCertifications
      ? `${entry.totalApprovedCertifications} approved certifications`
      : entry.employee.email,
  }))
}

function materialItems(data: DashboardData | null): DashboardListItem[] {
  return (data?.recentStudyMaterials ?? []).map((material) => ({
    id: material.id,
    primary: material.title,
    secondary: `${material.materialType} · ${material.downloadCount} downloads`,
  }))
}

function projectItems(projects: DashboardData['assignedProjects'] = []): DashboardListItem[] {
  return projects.map((project) => ({
    id: project.id,
    primary: project.name,
    secondary: `${project.accessType}${project.archived ? ' · Archived' : ''}`,
  }))
}

function formatNumber(value: number | undefined) {
  return value == null ? '--' : String(value)
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
