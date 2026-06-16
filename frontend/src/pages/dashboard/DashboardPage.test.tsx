import { render, screen, waitFor } from '@testing-library/react'
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
    },
  ],
  expiringInitiativesCount: 1,
  leaderboardPreview: [
    {
      employee: {
        email: 'learner@example.com',
        fullName: 'Top Learner',
        id: 'user-1',
      },
      rank: 1,
      totalApprovedCertifications: 4,
    },
  ],
  myRank: {
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
  pendingReviewsCount: 7,
  recentProjectUpdates: [
    {
      accessType: 'PUBLIC',
      archived: false,
      id: 'project-2',
      name: 'Observability',
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
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders admin dashboard widgets from admin API data', async () => {
    vi.mocked(useAuth).mockReturnValue({ isAdmin: true } as ReturnType<typeof useAuth>)
    vi.mocked(getAdminDashboardData).mockResolvedValue(dashboardData)

    render(<DashboardPage />)

    expect(screen.getByText('Admin Dashboard')).toBeInTheDocument()
    expect(screen.getByText('Active Initiatives')).toBeInTheDocument()

    await waitFor(() => expect(screen.getByText('Pending Reviews')).toBeInTheDocument())
    expect(screen.getByText('7')).toBeInTheDocument()
    expect(screen.getByText('Top Learners Preview')).toBeInTheDocument()
    expect(screen.getByText('#1 Top Learner')).toBeInTheDocument()
    expect(screen.getByText('Recent Project Updates')).toBeInTheDocument()
    expect(screen.getByText('Observability')).toBeInTheDocument()
    expect(getAdminDashboardData).toHaveBeenCalledTimes(1)
    expect(getEmployeeDashboardData).not.toHaveBeenCalled()
  })

  it('renders employee dashboard widgets from employee API data', async () => {
    vi.mocked(useAuth).mockReturnValue({ isAdmin: false } as ReturnType<typeof useAuth>)
    vi.mocked(getEmployeeDashboardData).mockResolvedValue(dashboardData)

    render(<DashboardPage />)

    expect(screen.getByText('Employee Dashboard')).toBeInTheDocument()

    await waitFor(() => expect(screen.getByText('My Rank')).toBeInTheDocument())
    expect(screen.getByText('#2')).toBeInTheDocument()
    expect(screen.getAllByText('My Submissions').length).toBeGreaterThan(0)
    expect(screen.getByText('Assigned Projects')).toBeInTheDocument()
    expect(screen.getByText('Payments Platform')).toBeInTheDocument()
    expect(getEmployeeDashboardData).toHaveBeenCalledTimes(1)
    expect(getAdminDashboardData).not.toHaveBeenCalled()
  })

  it('renders error state when dashboard loading fails', async () => {
    vi.mocked(useAuth).mockReturnValue({ isAdmin: false } as ReturnType<typeof useAuth>)
    vi.mocked(getEmployeeDashboardData).mockRejectedValue(new Error('network'))

    render(<DashboardPage />)

    expect(await screen.findByText('Unable to load dashboard data. Please refresh or try again later.')).toBeInTheDocument()
  })
})

