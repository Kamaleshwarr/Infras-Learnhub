import { chromium } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const outputDir = path.resolve(__dirname, '../../docs/screenshots/dashboard-enhancement')

const employeeUser = {
  id: 'employee-1',
  employeeId: 'EMP001',
  fullName: 'Employee One',
  email: 'employee@learninghub.local',
  roles: ['EMPLOYEE'],
  mustChangePassword: false,
}

const adminUser = {
  id: 'admin-1',
  employeeId: 'ADM001',
  fullName: 'Admin User',
  email: 'admin@learninghub.local',
  roles: ['ADMIN'],
  mustChangePassword: false,
}

const dashboardPayload = {
  activeInitiatives: [
    {
      id: 'initiative-1',
      title: 'AWS Solutions Architect',
      description: 'Cloud certification program',
      status: 'ACTIVE',
      startDateUtc: '2026-01-01T00:00:00Z',
      expiryDateUtc: '2026-12-31T00:00:00Z',
    },
    {
      id: 'initiative-2',
      title: 'Kubernetes Administrator',
      description: 'Container orchestration',
      status: 'ACTIVE',
      startDateUtc: '2026-02-01T00:00:00Z',
      expiryDateUtc: '2026-11-30T00:00:00Z',
    },
  ],
  activeInitiativesCount: 4,
  mySubmissions: [
    {
      id: 'submission-1',
      approvalStatus: 'APPROVED',
      submittedAtUtc: '2026-06-01T00:00:00Z',
      createdAtUtc: '2026-06-01T00:00:00Z',
      updatedAtUtc: '2026-06-01T00:00:00Z',
      certificateDocumentId: 'doc-1',
      certificateDocument: {
        id: 'doc-1',
        originalFilename: 'aws.pdf',
        contentType: 'application/pdf',
        fileSizeBytes: 1024,
      },
      employee: employeeUser,
      initiative: { id: 'initiative-1', title: 'AWS Solutions Architect', status: 'ACTIVE' },
    },
  ],
  mySubmissionsTotalCount: 3,
  pendingReviewsCount: 5,
  certificatesSubmittedCount: 18,
  totalUsersCount: 42,
  leaderboardPreview: [
    {
      rank: 1,
      totalApprovedCertifications: 6,
      earliestSubmittedAtUtc: '2026-05-01T00:00:00Z',
      latestApprovedAtUtc: '2026-05-10T00:00:00Z',
      employee: { id: 'u1', employeeId: 'EMP010', fullName: 'Alex Morgan', email: 'alex@example.com' },
    },
    {
      rank: 2,
      totalApprovedCertifications: 5,
      earliestSubmittedAtUtc: '2026-05-02T00:00:00Z',
      latestApprovedAtUtc: '2026-05-12T00:00:00Z',
      employee: employeeUser,
    },
  ],
  myRank: {
    employee: employeeUser,
    globalRank: 2,
    totalApprovedCertifications: 5,
    earliestSubmittedAtUtc: '2026-05-02T00:00:00Z',
    recentApprovals: [],
  },
  recentStudyMaterials: [
    {
      id: 'material-1',
      title: 'AWS Study Guide',
      materialType: 'PDF',
      sourceType: 'FILE',
      downloadCount: 12,
    },
  ],
  assignedProjects: [
    {
      id: 'project-1',
      name: 'Payments Platform',
      status: 'ACTIVE',
      accessType: 'MEMBERS_ONLY',
      archived: false,
    },
  ],
  recentProjectUpdates: [
    {
      id: 'project-2',
      name: 'Observability Stack',
      status: 'ACTIVE',
      accessType: 'PUBLIC',
      archived: false,
      updatedAtUtc: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    },
  ],
  recentNotifications: [
    {
      id: 'n1',
      type: 'CERTIFICATE_APPROVED',
      title: 'Certificate approved',
      message: 'Your AWS Solutions Architect submission was approved.',
      read: false,
      createdAtUtc: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      actionPath: '/submissions',
    },
    {
      id: 'n2',
      type: 'CERTIFICATE_SUBMITTED',
      title: 'Submission received',
      message: 'Your certificate is pending review.',
      read: true,
      createdAtUtc: new Date(Date.now() - 26 * 60 * 60 * 1000).toISOString(),
      actionPath: '/submissions',
    },
  ],
  unreadNotificationsCount: 1,
}

async function installApiMocks(page, user) {
  await page.route('**/api/v1/**', async (route) => {
    const url = route.request().url()
    const requestUrl = new URL(url)

    if (url.includes('/auth/me')) {
      return route.fulfill({ json: user })
    }
    if (url.includes('/notifications/unread-count')) {
      return route.fulfill({ json: { count: dashboardPayload.unreadNotificationsCount } })
    }
    if (url.includes('/notifications')) {
      return route.fulfill({
        json: {
          content: dashboardPayload.recentNotifications,
          page: 0,
          size: 5,
          totalElements: dashboardPayload.recentNotifications.length,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/initiatives')) {
      return route.fulfill({
        json: {
          content: dashboardPayload.activeInitiatives,
          page: 0,
          size: Number(requestUrl.searchParams.get('size') ?? 5),
          totalElements: dashboardPayload.activeInitiativesCount,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/me/submissions')) {
      return route.fulfill({
        json: {
          content: dashboardPayload.mySubmissions,
          page: 0,
          size: 5,
          totalElements: dashboardPayload.mySubmissionsTotalCount,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/submissions')) {
      const status = requestUrl.searchParams.get('status')
      return route.fulfill({
        json: {
          content: status === 'SUBMITTED' ? [] : dashboardPayload.mySubmissions,
          page: 0,
          size: Number(requestUrl.searchParams.get('size') ?? 5),
          totalElements:
            status === 'SUBMITTED' ? dashboardPayload.pendingReviewsCount : dashboardPayload.certificatesSubmittedCount,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/leaderboards/global')) {
      return route.fulfill({
        json: {
          content: dashboardPayload.leaderboardPreview,
          page: 0,
          size: 5,
          totalElements: dashboardPayload.leaderboardPreview.length,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/leaderboards/me')) {
      return route.fulfill({ json: dashboardPayload.myRank })
    }
    if (url.includes('/users')) {
      return route.fulfill({
        json: {
          content: [],
          page: 0,
          size: 1,
          totalElements: dashboardPayload.totalUsersCount,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/assistant/status')) {
      return route.fulfill({ json: { enabled: false, provider: 'mock', healthy: true } })
    }
    if (url.includes('/study-materials')) {
      return route.fulfill({
        json: {
          content: dashboardPayload.recentStudyMaterials,
          page: 0,
          size: 5,
          totalElements: dashboardPayload.recentStudyMaterials.length,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }
    if (url.includes('/projects')) {
      const assigned = requestUrl.searchParams.get('assigned') === 'true'
      const content = assigned ? dashboardPayload.assignedProjects : dashboardPayload.recentProjectUpdates
      return route.fulfill({
        json: {
          content,
          page: 0,
          size: 5,
          totalElements: content.length,
          totalPages: 1,
          first: true,
          last: true,
          sort: [],
        },
      })
    }

    return route.fulfill({ status: 404, json: { message: 'Not mocked' } })
  })
}

async function captureRole(baseUrl, label, user) {
  const browser = await chromium.launch()
  const page = await browser.newPage({ viewport: { width: 1440, height: 1200 } })
  await installApiMocks(page, user)
  await page.addInitScript(() => {
    sessionStorage.setItem('elh.accessToken', 'mock-token')
  })
  await page.goto(`${baseUrl}/`)
  await page.waitForSelector('text=/Dashboard|Active Initiatives|Quick Statistics/', { timeout: 20000 })
  await page.waitForTimeout(1200)
  await page.screenshot({ path: path.join(outputDir, `${label}.png`), fullPage: true })
  await browser.close()
}

const baseUrl = process.argv[2]
const labelPrefix = process.argv[3] ?? 'dashboard'

if (!baseUrl) {
  console.error('Usage: node dashboard-screenshot.mjs <preview-url> [label-prefix]')
  process.exit(1)
}

await mkdir(outputDir, { recursive: true })
await captureRole(baseUrl, `${labelPrefix}-employee-dashboard`, employeeUser)
await captureRole(baseUrl, `${labelPrefix}-admin-dashboard`, adminUser)
console.log(`Saved screenshots to ${outputDir}`)
