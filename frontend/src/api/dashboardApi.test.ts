import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getEmployeeDashboardData } from './dashboardApi'
import { initiativesApi } from './initiativesApi'
import { leaderboardsApi } from './leaderboardsApi'
import { projectsApi } from './projectsApi'
import { studyMaterialsApi } from './studyMaterialsApi'
import { submissionsApi } from './submissionsApi'

vi.mock('./initiativesApi', () => ({
  initiativesApi: { list: vi.fn() },
}))

vi.mock('./submissionsApi', () => ({
  submissionsApi: { listMine: vi.fn() },
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

describe('getEmployeeDashboardData', () => {
  beforeEach(() => {
    vi.clearAllMocks()
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
    vi.mocked(submissionsApi.listMine).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 5,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
    vi.mocked(leaderboardsApi.global).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 5,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
    vi.mocked(leaderboardsApi.me).mockResolvedValue({
      globalRank: null,
      recentApprovals: [],
      totalApprovedCertifications: 0,
    })
    vi.mocked(studyMaterialsApi.search).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 5,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
    vi.mocked(projectsApi.list).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 5,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
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
