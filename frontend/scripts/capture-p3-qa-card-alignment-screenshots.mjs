import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const outputDir = process.argv[2] ?? '../docs/screenshots/p3-qa-card-alignment'
const prefix = process.argv[3] ?? 'p3-qa'

const viewports = [
  { name: 'desktop', width: 1280, height: 900 },
  { name: 'tablet', width: 834, height: 1100 },
  { name: 'mobile', width: 390, height: 1200 },
]

async function fetchAccessToken(email, password) {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: { email, password },
    headers: { 'Content-Type': 'application/json' },
  })
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()} ${await response.text()}`)
  }
  const body = await response.json()
  await api.dispose()
  return body.accessToken
}

async function ensureAlignmentProject(adminToken) {
  const api = await request.newContext()
  const headers = { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' }

  const create = await api.post(`${apiUrl}/projects`, {
    data: {
      name: `P3 QA Alignment ${Date.now()}`,
      description: 'Mixed-content card alignment verification',
      accessType: 'PUBLIC',
    },
    headers,
  })
  const project = await create.json()
  const projectId = project.id

  const repos = [
    {
      name: 'Enterprise Payments Platform Backend Monorepo Service',
      description: 'Primary Spring Boot backend with long descriptive text for clamp verification.',
      repositoryType: 'BACKEND',
      provider: 'GITHUB',
      repositoryUrl: 'https://github.com/example/backend',
      defaultBranch: 'main',
    },
    {
      name: 'UI',
      repositoryType: 'FRONTEND',
      provider: 'GITHUB',
      repositoryUrl: 'https://github.com/example/frontend',
    },
    {
      name: 'Automation Framework',
      description: 'Playwright and API automation suite.',
      repositoryType: 'AUTOMATION',
      provider: 'GITHUB',
      repositoryUrl: 'https://github.com/example/automation',
    },
    {
      name: 'Infrastructure',
      repositoryType: 'INFRASTRUCTURE',
      provider: 'GITLAB',
      repositoryUrl: 'https://gitlab.com/example/infra',
      defaultBranch: 'develop',
    },
  ]

  for (const repo of repos) {
    await api.post(`${apiUrl}/projects/${projectId}/repositories`, { data: repo, headers })
  }

  const rootFolder = await api.post(`${apiUrl}/projects/${projectId}/folders`, {
    data: { name: 'Alignment QA', description: 'Folder for mixed resource cards' },
    headers,
  })
  const folder = await rootFolder.json()

  const items = [
    {
      title: 'Enterprise Architecture Decision Record for Payments Platform Modernization',
      description: 'Long-form ADR stored in Confluence with detailed rationale.',
      category: 'ARCHITECTURE',
      externalUrl: 'https://confluence.example.com/adr-payments',
    },
    {
      title: 'Runbook',
      category: 'OPERATIONS',
      externalUrl: 'https://confluence.example.com/runbook',
    },
    {
      title: 'API Spec',
      description: 'OpenAPI specification.',
      category: 'API',
      externalUrl: 'https://example.com/openapi',
    },
    {
      title: 'Wiki',
      category: 'DOCUMENTATION',
      externalUrl: 'https://wiki.example.com/project',
    },
  ]

  for (const item of items) {
    await api.post(`${apiUrl}/projects/${projectId}/items/links`, {
      data: { ...item, folderId: folder.id },
      headers,
    })
  }

  await api.dispose()
  return { projectId, folderId: folder.id }
}

async function capture() {
  await mkdir(outputDir, { recursive: true })
  const adminToken = await fetchAccessToken('admin@learninghub.local', 'Admin@12345')
  const { projectId, folderId } = await ensureAlignmentProject(adminToken)
  const browser = await chromium.launch()
  const context = await browser.newContext()
  const page = await context.newPage()

  await page.route('**/api/v1/**', async (route) => {
    const upstream = route.request().url().replace(`${appUrl}/api/v1`, apiUrl)
    const response = await route.fetch({ url: upstream })
    await route.fulfill({ response })
  })

  await page.addInitScript((token) => {
    window.sessionStorage.setItem('elh.accessToken', token)
  }, adminToken)

  const pages = [
    { slug: 'repositories', path: `/projects/${projectId}/repositories`, waitFor: /Repositories/i },
    {
      slug: 'knowledge-base',
      path: `/projects/${projectId}/knowledge/folders/${folderId}`,
      waitFor: /Resources/i,
    },
    { slug: 'environments', path: `/projects/${projectId}/environments`, waitFor: /Environments/i },
  ]

  for (const target of pages) {
    for (const viewport of viewports) {
      if (target.slug === 'environments' && viewport.name === 'tablet') continue
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.goto(`${appUrl}${target.path}`)
      await page.getByRole('heading', { name: target.waitFor }).waitFor({ timeout: 15000 })
      const file = path.join(outputDir, `${prefix}-${target.slug}-${viewport.name}.png`)
      await page.screenshot({ path: file, fullPage: true })
      console.log('saved', file)
    }
  }

  await browser.close()
}

capture().catch((error) => {
  console.error(error)
  process.exit(1)
})
