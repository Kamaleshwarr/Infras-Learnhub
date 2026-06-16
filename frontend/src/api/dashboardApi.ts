import { initiativesApi } from './initiativesApi'
import { leaderboardsApi } from './leaderboardsApi'
import { projectsApi } from './projectsApi'
import { studyMaterialsApi } from './studyMaterialsApi'
import { submissionsApi } from './submissionsApi'
import type { InitiativeSummary } from './initiativesApi'
import type { LeaderboardEntry, PersonalLeaderboard } from './leaderboardsApi'
import type { ProjectSummary } from './projectsApi'
import type { StudyMaterial } from './studyMaterialsApi'
import type { CertificateSubmission } from '../types/submissions'

const EXPIRING_WINDOW_DAYS = 14

export interface DashboardData {
  activeInitiatives: InitiativeSummary[]
  activeInitiativesCount: number
  expiringInitiativesCount: number
  mySubmissions: CertificateSubmission[]
  pendingReviewsCount: number
  leaderboardPreview: LeaderboardEntry[]
  myRank: PersonalLeaderboard | null
  recentStudyMaterials: StudyMaterial[]
  assignedProjects: ProjectSummary[]
  recentProjectUpdates: ProjectSummary[]
}

export async function getAdminDashboardData(): Promise<DashboardData> {
  const [initiatives, pendingSubmissions, leaderboard, materials, projects] = await Promise.all([
    initiativesApi.list({ size: 50, status: 'ACTIVE', sort: 'expiryDateUtc,asc' }),
    submissionsApi.listAll({ size: 1, status: 'SUBMITTED' }),
    leaderboardsApi.global({ size: 5, sort: 'rank,asc' }),
    studyMaterialsApi.search(undefined, { size: 5, sort: 'createdAtUtc,desc' }),
    projectsApi.list(undefined, { size: 5, sort: 'updatedAtUtc,desc' }),
  ])

  return {
    activeInitiatives: initiatives.content.slice(0, 5),
    activeInitiativesCount: initiatives.totalElements,
    expiringInitiativesCount: countExpiringInitiatives(initiatives.content),
    mySubmissions: [],
    pendingReviewsCount: pendingSubmissions.totalElements,
    leaderboardPreview: leaderboard.content,
    myRank: null,
    recentStudyMaterials: materials.content,
    assignedProjects: [],
    recentProjectUpdates: projects.content,
  }
}

export async function getEmployeeDashboardData(): Promise<DashboardData> {
  const [initiatives, submissions, leaderboard, myRank, materials, projects] = await Promise.all([
    initiativesApi.list({ size: 5, status: 'ACTIVE', sort: 'expiryDateUtc,asc' }),
    submissionsApi.listMine({ size: 5, sort: 'submittedAtUtc,desc' }),
    leaderboardsApi.global({ size: 5, sort: 'rank,asc' }),
    leaderboardsApi.me(),
    studyMaterialsApi.search(undefined, { size: 5, sort: 'createdAtUtc,desc' }),
    projectsApi.list(undefined, { size: 5, sort: 'updatedAtUtc,desc' }),
  ])

  return {
    activeInitiatives: initiatives.content,
    activeInitiativesCount: initiatives.totalElements,
    expiringInitiativesCount: countExpiringInitiatives(initiatives.content),
    mySubmissions: submissions.content,
    pendingReviewsCount: 0,
    leaderboardPreview: leaderboard.content,
    myRank,
    recentStudyMaterials: materials.content,
    assignedProjects: projects.content,
    recentProjectUpdates: [],
  }
}

function countExpiringInitiatives(initiatives: InitiativeSummary[]) {
  const now = Date.now()
  const threshold = now + EXPIRING_WINDOW_DAYS * 24 * 60 * 60 * 1000
  return initiatives.filter((initiative) => {
    const expiry = new Date(initiative.expiryDateUtc).getTime()
    return Number.isFinite(expiry) && expiry >= now && expiry <= threshold
  }).length
}

