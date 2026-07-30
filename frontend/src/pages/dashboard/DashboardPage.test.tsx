import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { DashboardPage } from './DashboardPage'
import { getAdminDashboardData, getEmployeeDashboardData } from '../../api/dashboardApi'
import { useAuth } from '../../auth/useAuth'
import type { DashboardData } from '../../api/dashboardApi'

vi.mock('../../api/dashboardApi', () => ({
  getAdminDashboardData: vi.fn(),
  getEmployeeDashboardData: vi.fn(),
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

const dashboardData: DashboardData = {
  activeInitiatives: [
    {
      description: 'AI certification',
      expiryDateUtc: '2026-12-31T00:00:00Z',
      id: 'initiative-1',
      startDateUtc: '2026-01-01T00:00:00Z',
      status: 'ACTIVE',
      title: 'AI Certification',
    },
  ],
  activeInitiativesCount: 3,
  assignedProjects: [
    {
      accessType: 'MEMBERS_ONLY',
      archived: false,
      id: 'project-1',
      name: 'Payments Platform',
      status: 'ACTIVE',
    },
  ],
  certificatesSubmittedCount: 15,
  expiringInitiativesCount: 1,
  leaderboardPreview: [
    {
      earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
      employee: {
        email: 'learner@example.com',
        employeeId: 'EMP002',
        fullName: 'Top Learner',
        id: 'user-1',
      },
      latestApprovedAtUtc: '2026-06-05T00:00:00Z',
      rank: 1,
      totalApprovedCertifications: 4,
    },
  ],
  myRank: {
    earliestSubmittedAtUtc: '2026-06-01T00:00:00Z',
    employee: {
      email: 'employee@example.com',
      employeeId: 'EMP001',
      fullName: 'Employee One',
      id: 'employee-1',
    },
    globalRank: 2,
    recentApprovals: [],
    totalApprovedCertifications: 5,
  },
  mySubmissions: [
    {
      approvalStatus: 'APPROVED',
      certificateDocument: {
        contentType: 'application/pdf',
        fileSizeBytes: 1024,
        id: 'document-1',
        originalFilename: 'certificate.pdf',
      },
      certificateDocumentId: 'document-1',
      createdAtUtc: '2026-06-01T00:00:00Z',
      employee: {
        email: 'employee@example.com',
        employeeId: 'EMP001',
        fullName: 'Employee One',
        id: 'employee-1',
      },
      id: 'submission-1',
      initiative: {
        id: 'initiative-1',
        status: 'ACTIVE',
        title: 'AI Certification',
      },
      submittedAtUtc: '2026-06-01T00:00:00Z',
      updatedAtUtc: '2026-06-01T00:00:00Z',
    },
  ],
  mySubmissionsTotalCount: 1,
  pendingReviewsCount: 7,
  recentActivity: [
    {
      description: 'A new certificate was submitted.',
      href: '/submissions/review',
      id: 'notification-1',
      timestamp: '2026-06-11T00:00:00Z',
      title: 'Certificate submitted',
    },
  ],
  recentNotifications: [
    {
      actionPath: '/submissions/review',
      createdAtUtc: '2026-06-11T00:00:00Z',
      id: 'notification-1',
      message: 'A new certificate was submitted.',
      read: false,
      title: 'Certificate submitted',
      type: 'CERTIFICATE_SUBMITTED',
    },
  ],
  recentProjectUpdates: [
    {
      accessType: 'PUBLIC',
      archived: false,
      id: 'project-2',
      name: 'Observability',
      status: 'ACTIVE',
    },
  ],
  recentStudyMaterials: [
    {
      downloadCount: 3,
      id: 'material-1',
      materialType: 'PDF',
      sourceType: 'FILE',
      title: 'AWS Guide',
    },
  ],
  totalUsersCount: 24,
  unreadNotificationsCount: 2,
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders admin dashboard sections from admin API data', async () => {
    vi.mocked(useAuth).mockReturnValue({
      isAdmin: true,
      user: { fullName: 'Admin User' },
    } as ReturnType<typeof useAuth>)
    vi.mocked(getAdminDashboardData).mockResolvedValue(dashboardData)

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    )

    expect(screen.getByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText(/Good (morning|afternoon|evening), Admin/)).toBeInTheDocument()
    expect(screen.getByText('Quick Statistics')).toBeInTheDocument()

    await waitFor(() => expect(screen.getByText('Total Users')).toBeInTheDocument())
    expect(screen.getByText('24')).toBeInTheDocument()
    expect(screen.getByText('Certificates Submitted')).toBeInTheDocument()
    expect(screen.getByText('15')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'View 7 pending certificate reviews' })).toHaveAttribute(
      'href',
      '/submissions/review',
    )
    expect(screen.getByText('Leaderboard Snapshot')).toBeInTheDocument()
    expect(screen.getByText('Recent Notifications')).toBeInTheDocument()
    expect(screen.getByText('Quick Actions')).toBeInTheDocument()
    expect(screen.getByText('Recent Activity')).toBeInTheDocument()
    expect(getAdminDashboardData).toHaveBeenCalledTimes(1)
    expect(getEmployeeDashboardData).not.toHaveBeenCalled()
  })

  it('renders employee dashboard sections from employee API data', async () => {
    vi.mocked(useAuth).mockReturnValue({
      isAdmin: false,
      user: { fullName: 'Employee One' },
    } as ReturnType<typeof useAuth>)
    vi.mocked(getEmployeeDashboardData).mockResolvedValue(dashboardData)

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    )

    expect(screen.getByText(/Good (morning|afternoon|evening), Employee/)).toBeInTheDocument()

    await waitFor(() => expect(screen.getByText('My Active Initiatives')).toBeInTheDocument())
    expect(screen.getAllByText('My Certificates').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Leaderboard Rank').length).toBeGreaterThan(0)
    expect(screen.getAllByText('#2').length).toBeGreaterThan(0)
    expect(screen.getByText('Recent Notifications')).toBeInTheDocument()
    expect(screen.getByText('Quick Actions')).toBeInTheDocument()
    expect(screen.getByText('Submit Certificate')).toBeInTheDocument()
    expect(getEmployeeDashboardData).toHaveBeenCalledTimes(1)
    expect(getAdminDashboardData).not.toHaveBeenCalled()
  })

  it('renders error state when dashboard loading fails', async () => {
    vi.mocked(useAuth).mockReturnValue({ isAdmin: false } as ReturnType<typeof useAuth>)
    vi.mocked(getEmployeeDashboardData).mockRejectedValue(new Error('network'))

    render(
      <MemoryRouter>
        <DashboardPage />
      </MemoryRouter>,
    )

    expect(await screen.findByText('Unable to load dashboard data. Please refresh or try again later.')).toBeInTheDocument()
  })
})
