import { initiativesApi } from './initiativesApi'
import { leaderboardsApi } from './leaderboardsApi'
import { notificationsApi } from './notificationsApi'
import { projectsApi } from './projectsApi'
import { studyMaterialsApi } from './studyMaterialsApi'
import { submissionsApi } from './submissionsApi'
import { usersApi } from './usersApi'
import type { Initiative } from '../types/initiatives'
import type { GlobalLeaderboardEntry, PersonalLeaderboard, RecentApproval } from '../types/leaderboards'
import type { Notification } from '../types/notifications'
import type { ProjectSummary } from './projectsApi'
import type { StudyMaterial } from './studyMaterialsApi'
import type { CertificateSubmission } from '../types/submissions'

const EXPIRING_WINDOW_DAYS = 14

export interface DashboardActivityItem {
  id: string
  title: string
  description: string
  timestamp: string
  href?: string
}

export interface DashboardData {
  activeInitiatives: Initiative[]
  activeInitiativesCount: number
  expiringInitiativesCount: number
  mySubmissions: CertificateSubmission[]
  mySubmissionsTotalCount: number
  pendingReviewsCount: number
  certificatesSubmittedCount: number
  totalUsersCount: number
  leaderboardPreview: GlobalLeaderboardEntry[]
  myRank: PersonalLeaderboard | null
  recentStudyMaterials: StudyMaterial[]
  assignedProjects: ProjectSummary[]
  recentProjectUpdates: ProjectSummary[]
  recentNotifications: Notification[]
  unreadNotificationsCount: number
  recentActivity: DashboardActivityItem[]
}

export async function getAdminDashboardData(): Promise<DashboardData> {
  const initiativesResult = await initiativesApi
    .list({ size: 50, status: 'ACTIVE', sort: 'expiryDateUtc,asc' })
    .then((value) => ({ ok: true as const, value }))
    .catch(() => ({ ok: false as const, value: null }))

  const pendingSubmissionsResult = await submissionsApi
    .listAll({ size: 1, status: 'SUBMITTED' })
    .then((value) => ({ ok: true as const, value }))
    .catch(() => ({ ok: false as const, value: null }))

  if (!initiativesResult.ok && !pendingSubmissionsResult.ok) {
    throw new Error('Unable to load admin dashboard primary data')
  }

  const [
    leaderboardResult,
    materialsResult,
    projectsResult,
    usersResult,
    allSubmissionsResult,
    recentSubmissionsResult,
    notificationsResult,
    unreadCountResult,
  ] = await Promise.allSettled([
    leaderboardsApi.global({ size: 5, sort: 'rank,asc' }),
    studyMaterialsApi.search(undefined, { size: 5, sort: 'createdAtUtc,desc' }),
    projectsApi.list({ size: 5, sort: 'updatedAtUtc,desc' }),
    usersApi.list({ size: 1, sort: 'fullName,asc' }),
    submissionsApi.listAll({ size: 1, sort: 'submittedAtUtc,desc' }),
    submissionsApi.listAll({ size: 5, sort: 'submittedAtUtc,desc' }),
    notificationsApi.list({ size: 5, sort: 'createdAt,desc' }),
    notificationsApi.unreadCount(),
  ])

  const initiatives = initiativesResult.ok ? initiativesResult.value : null
  const pendingSubmissions = pendingSubmissionsResult.ok ? pendingSubmissionsResult.value : null
  const recentNotifications = notificationsResult.status === 'fulfilled' ? notificationsResult.value.content : []
  const recentSubmissions =
    recentSubmissionsResult.status === 'fulfilled' ? recentSubmissionsResult.value.content : []
  const recentProjectUpdates = projectsResult.status === 'fulfilled' ? projectsResult.value.content : []

  return {
    activeInitiatives: initiatives?.content.slice(0, 5) ?? [],
    activeInitiativesCount: initiatives?.totalElements ?? 0,
    expiringInitiativesCount: countExpiringInitiatives(initiatives?.content ?? []),
    mySubmissions: [],
    mySubmissionsTotalCount: 0,
    pendingReviewsCount: pendingSubmissions?.totalElements ?? 0,
    certificatesSubmittedCount:
      allSubmissionsResult.status === 'fulfilled' ? allSubmissionsResult.value.totalElements : 0,
    totalUsersCount: usersResult.status === 'fulfilled' ? usersResult.value.totalElements : 0,
    leaderboardPreview: leaderboardResult.status === 'fulfilled' ? leaderboardResult.value.content : [],
    myRank: null,
    recentStudyMaterials: materialsResult.status === 'fulfilled' ? materialsResult.value.content : [],
    assignedProjects: [],
    recentProjectUpdates,
    recentNotifications,
    unreadNotificationsCount: unreadCountResult.status === 'fulfilled' ? unreadCountResult.value.count : 0,
    recentActivity: buildAdminActivity(recentNotifications, recentSubmissions, recentProjectUpdates),
  }
}

export async function getEmployeeDashboardData(): Promise<DashboardData> {
  const initiativesResult = await initiativesApi
    .list({ size: 5, status: 'ACTIVE', sort: 'expiryDateUtc,asc' })
    .then((value) => ({ ok: true as const, value }))
    .catch(() => ({ ok: false as const, value: null }))

  const [
    submissionsResult,
    leaderboardResult,
    myRankResult,
    materialsResult,
    projectsResult,
    notificationsResult,
    unreadCountResult,
  ] = await Promise.allSettled([
    submissionsApi.listMine({ size: 5, sort: 'submittedAtUtc,desc' }),
    leaderboardsApi.global({ size: 5, sort: 'rank,asc' }),
    leaderboardsApi.me(),
    studyMaterialsApi.search(undefined, { size: 5, sort: 'createdAtUtc,desc' }),
    projectsApi.list({ assigned: true, size: 5, sort: 'updatedAtUtc,desc' }),
    notificationsApi.list({ size: 5, sort: 'createdAt,desc' }),
    notificationsApi.unreadCount(),
  ])

  const initiatives = initiativesResult.ok ? initiativesResult.value : null
  const mySubmissions = submissionsResult.status === 'fulfilled' ? submissionsResult.value.content : []
  const myRank = myRankResult.status === 'fulfilled' ? myRankResult.value : null
  const recentNotifications = notificationsResult.status === 'fulfilled' ? notificationsResult.value.content : []

  return {
    activeInitiatives: initiatives?.content ?? [],
    activeInitiativesCount: initiatives?.totalElements ?? 0,
    expiringInitiativesCount: countExpiringInitiatives(initiatives?.content ?? []),
    mySubmissions,
    mySubmissionsTotalCount: submissionsResult.status === 'fulfilled' ? submissionsResult.value.totalElements : 0,
    pendingReviewsCount: 0,
    certificatesSubmittedCount: 0,
    totalUsersCount: 0,
    leaderboardPreview: leaderboardResult.status === 'fulfilled' ? leaderboardResult.value.content : [],
    myRank,
    recentStudyMaterials: materialsResult.status === 'fulfilled' ? materialsResult.value.content : [],
    assignedProjects: projectsResult.status === 'fulfilled' ? projectsResult.value.content : [],
    recentProjectUpdates: [],
    recentNotifications,
    unreadNotificationsCount: unreadCountResult.status === 'fulfilled' ? unreadCountResult.value.count : 0,
    recentActivity: buildEmployeeActivity(mySubmissions, myRank?.recentApprovals ?? [], recentNotifications),
  }
}

function countExpiringInitiatives(initiatives: Initiative[]) {
  const now = Date.now()
  const threshold = now + EXPIRING_WINDOW_DAYS * 24 * 60 * 60 * 1000
  return initiatives.filter((initiative) => {
    const expiry = new Date(initiative.expiryDateUtc).getTime()
    return Number.isFinite(expiry) && expiry >= now && expiry <= threshold
  }).length
}

function buildAdminActivity(
  notifications: Notification[],
  submissions: CertificateSubmission[],
  projects: ProjectSummary[],
): DashboardActivityItem[] {
  const items: DashboardActivityItem[] = [
    ...notifications.map((notification) => ({
      id: `notification-${notification.id}`,
      title: notification.title,
      description: notification.message,
      timestamp: notification.createdAtUtc,
      href: notification.actionPath ?? '/notifications',
    })),
    ...submissions.map((submission) => ({
      id: `submission-${submission.id}`,
      title: `Certificate ${submission.approvalStatus.toLowerCase()}`,
      description: `${submission.employee.fullName} · ${submission.initiative.title}`,
      timestamp: submission.submittedAtUtc,
      href: '/submissions/review',
    })),
    ...projects
      .filter((project) => project.updatedAtUtc)
      .map((project) => ({
        id: `project-${project.id}`,
        title: 'Project updated',
        description: project.name,
        timestamp: project.updatedAtUtc!,
        href: `/projects/${project.id}`,
      })),
  ]

  return sortActivityItems(items).slice(0, 8)
}

function buildEmployeeActivity(
  submissions: CertificateSubmission[],
  recentApprovals: RecentApproval[],
  notifications: Notification[],
): DashboardActivityItem[] {
  const items: DashboardActivityItem[] = [
    ...notifications.map((notification) => ({
      id: `notification-${notification.id}`,
      title: notification.title,
      description: notification.message,
      timestamp: notification.createdAtUtc,
      href: notification.actionPath ?? '/notifications',
    })),
    ...submissions.map((submission) => ({
      id: `submission-${submission.id}`,
      title: `Certificate ${submission.approvalStatus.toLowerCase()}`,
      description: submission.initiative.title,
      timestamp: submission.submittedAtUtc,
      href: '/submissions',
    })),
    ...recentApprovals.map((approval) => ({
      id: `approval-${approval.submissionId}`,
      title: 'Certification approved',
      description: approval.initiativeTitle,
      timestamp: approval.approvedAtUtc,
      href: '/submissions',
    })),
  ]

  return sortActivityItems(items).slice(0, 8)
}

function sortActivityItems(items: DashboardActivityItem[]) {
  return [...items].sort((left, right) => Date.parse(right.timestamp) - Date.parse(left.timestamp))
}
