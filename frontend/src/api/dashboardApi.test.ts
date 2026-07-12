import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getAdminDashboardData, getEmployeeDashboardData } from './dashboardApi'
import { initiativesApi } from './initiativesApi'
import { leaderboardsApi } from './leaderboardsApi'
import { projectsApi } from './projectsApi'
import { studyMaterialsApi } from './studyMaterialsApi'
import { submissionsApi } from './submissionsApi'

vi.mock('./initiativesApi', () => ({
  initiativesApi: { list: vi.fn() },
}))

vi.mock('./submissionsApi', () => ({
  submissionsApi: { listMine: vi.fn(), listAll: vi.fn() },
}))

vi.mock('./leaderboardsApi', () => ({
  leaderboardsApi: { global: vi.fn(), me: vi.fn() },
}))

vi.mock('./studyMaterialsApi', () => ({
  studyMaterialsApi: { search: vi.fn() },
}))

vi.mock('./projectsApi', () => ({
  projectsApi: { list: vi.fn() },
}))

const initiative = {
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: '550e8400-e29b-41d4-a716-446655440001',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

const leaderboardEntry = {
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
}

const studyMaterial = {
  downloadCount: 3,
  id: 'material-1',
  materialType: 'PDF' as const,
  sourceType: 'FILE' as const,
  title: 'AWS Guide',
}

const project = {
  accessType: 'PUBLIC' as const,
  archived: false,
  status: 'ACTIVE' as const,
  id: 'project-1',
  name: 'Observability',
}

const emptyPage = {
  content: [],
  first: true,
  last: true,
  page: 0,
  size: 5,
  sort: [],
  totalElements: 0,
  totalPages: 0,
}

function mockAdminDefaults() {
  vi.mocked(initiativesApi.list).mockResolvedValue({
    content: [initiative],
    first: true,
    last: true,
    page: 0,
    size: 50,
    sort: [],
    totalElements: 3,
    totalPages: 1,
  })
  vi.mocked(submissionsApi.listAll).mockResolvedValue({
    ...emptyPage,
    totalElements: 7,
  })
  vi.mocked(leaderboardsApi.global).mockResolvedValue({
    content: [leaderboardEntry],
    first: true,
    last: true,
    page: 0,
    size: 5,
    sort: [],
    totalElements: 1,
    totalPages: 1,
  })
  vi.mocked(studyMaterialsApi.search).mockResolvedValue({
    content: [studyMaterial],
    first: true,
    last: true,
    page: 0,
    size: 5,
    sort: [],
    totalElements: 1,
    totalPages: 1,
  })
  vi.mocked(projectsApi.list).mockResolvedValue({
    content: [project],
    first: true,
    last: true,
    page: 0,
    size: 5,
    sort: [],
    totalElements: 1,
    totalPages: 1,
  })
}

function mockEmployeeDefaults() {
  vi.mocked(initiativesApi.list).mockResolvedValue({
    content: [initiative],
    first: true,
    last: true,
    page: 0,
    size: 5,
    sort: [],
    totalElements: 1,
    totalPages: 1,
  })
  vi.mocked(submissionsApi.listMine).mockResolvedValue(emptyPage)
  vi.mocked(leaderboardsApi.global).mockResolvedValue(emptyPage)
  vi.mocked(leaderboardsApi.me).mockResolvedValue({
    earliestSubmittedAtUtc: null,
    employee: {
      email: 'employee@example.com',
      employeeId: 'EMP001',
      fullName: 'Employee One',
      id: 'employee-1',
    },
    globalRank: null,
    recentApprovals: [],
    totalApprovedCertifications: 0,
  })
  vi.mocked(studyMaterialsApi.search).mockResolvedValue(emptyPage)
  vi.mocked(projectsApi.list).mockResolvedValue(emptyPage)
}

describe('getAdminDashboardData', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockAdminDefaults()
  })

  it('returns primary metrics when secondary dashboard APIs fail', async () => {
    vi.mocked(leaderboardsApi.global).mockRejectedValue(new Error('leaderboard unavailable'))
    vi.mocked(studyMaterialsApi.search).mockRejectedValue(new Error('study materials unavailable'))
    vi.mocked(projectsApi.list).mockRejectedValue(new Error('projects unavailable'))

    const data = await getAdminDashboardData()

    expect(data.activeInitiativesCount).toBe(3)
    expect(data.pendingReviewsCount).toBe(7)
    expect(data.leaderboardPreview).toEqual([])
    expect(data.recentStudyMaterials).toEqual([])
    expect(data.recentProjectUpdates).toEqual([])
  })

  it('still returns initiatives when leaderboard API fails', async () => {
    vi.mocked(leaderboardsApi.global).mockRejectedValue(new Error('leaderboard unavailable'))

    const data = await getAdminDashboardData()

    expect(data.activeInitiatives).toEqual([initiative])
    expect(data.activeInitiativesCount).toBe(3)
    expect(data.leaderboardPreview).toEqual([])
    expect(data.pendingReviewsCount).toBe(7)
  })

  it('still returns pending reviews when study materials API fails', async () => {
    vi.mocked(studyMaterialsApi.search).mockRejectedValue(new Error('study materials unavailable'))

    const data = await getAdminDashboardData()

    expect(data.pendingReviewsCount).toBe(7)
    expect(data.recentStudyMaterials).toEqual([])
    expect(data.activeInitiativesCount).toBe(3)
  })

  it('still returns primary metrics when projects API fails', async () => {
    vi.mocked(projectsApi.list).mockRejectedValue(new Error('projects unavailable'))

    const data = await getAdminDashboardData()

    expect(data.activeInitiativesCount).toBe(3)
    expect(data.pendingReviewsCount).toBe(7)
    expect(data.recentProjectUpdates).toEqual([])
  })

  it('returns initiatives when submissions API fails without throwing', async () => {
    vi.mocked(submissionsApi.listAll).mockRejectedValue(new Error('submissions unavailable'))

    const data = await getAdminDashboardData()

    expect(data.activeInitiativesCount).toBe(3)
    expect(data.pendingReviewsCount).toBe(0)
  })

  it('returns pending reviews when initiatives API fails without throwing', async () => {
    vi.mocked(initiativesApi.list).mockRejectedValue(new Error('initiatives unavailable'))

    const data = await getAdminDashboardData()

    expect(data.activeInitiatives).toEqual([])
    expect(data.activeInitiativesCount).toBe(0)
    expect(data.pendingReviewsCount).toBe(7)
  })

  it('throws only when both primary dashboard APIs fail', async () => {
    vi.mocked(initiativesApi.list).mockRejectedValue(new Error('initiatives unavailable'))
    vi.mocked(submissionsApi.listAll).mockRejectedValue(new Error('submissions unavailable'))

    await expect(getAdminDashboardData()).rejects.toThrow('Unable to load admin dashboard primary data')
  })
})

describe('getEmployeeDashboardData', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockEmployeeDefaults()
  })

  it('still returns initiatives when other dashboard APIs fail', async () => {
    vi.mocked(leaderboardsApi.me).mockRejectedValue(new Error('leaderboard unavailable'))
    vi.mocked(projectsApi.list).mockRejectedValue(new Error('projects unavailable'))

    const data = await getEmployeeDashboardData()

    expect(data.activeInitiatives).toEqual([initiative])
    expect(data.activeInitiativesCount).toBe(1)
    expect(data.myRank).toBeNull()
    expect(data.assignedProjects).toEqual([])
  })

  it('returns empty initiatives when initiative API fails without throwing', async () => {
    vi.mocked(initiativesApi.list).mockRejectedValue(new Error('initiatives unavailable'))

    const data = await getEmployeeDashboardData()

    expect(data.activeInitiatives).toEqual([])
    expect(data.activeInitiativesCount).toBe(0)
  })
})
