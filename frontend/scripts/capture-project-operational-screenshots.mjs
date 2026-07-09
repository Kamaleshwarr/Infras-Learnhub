import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const outputDir = process.argv[2] ?? '../docs/screenshots/p3-environments-repositories'
const prefix = process.argv[3] ?? 'p3'

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

async function ensureOperationalProject(adminToken) {
  const api = await request.newContext()
  const create = await api.post(`${apiUrl}/projects`, {
    data: {
      name: `P3 Screenshot Project ${Date.now()}`,
      description: 'Sample project for P3 environment and repository screenshots',
      accessType: 'PUBLIC',
    },
    headers: {
      Authorization: `Bearer ${adminToken}`,
      'Content-Type': 'application/json',
    },
  })
  const project = await create.json()
  const projectId = project.id

  const qa = await api.post(`${apiUrl}/projects/${projectId}/environments`, {
    data: { name: 'QA', description: 'Quality assurance environment' },
    headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
  })
  const qaBody = await qa.json()

  await api.post(`${apiUrl}/projects/${projectId}/environments/${qaBody.id}/references`, {
    data: {
      name: 'Swagger',
      referenceType: 'SWAGGER',
      url: 'https://example.com/swagger',
    },
    headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
  })

  await api.post(`${apiUrl}/projects/${projectId}/repositories`, {
    data: {
      name: 'Backend Service',
      description: 'Spring Boot backend',
      repositoryType: 'BACKEND',
      provider: 'GITHUB',
      repositoryUrl: 'https://github.com/example/backend',
      defaultBranch: 'main',
    },
    headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
  })

  await api.dispose()
  return projectId
}

async function capture() {
  await mkdir(outputDir, { recursive: true })
  const adminToken = await fetchAccessToken('admin@learninghub.local', 'Admin@12345')
  const projectId = await ensureOperationalProject(adminToken)
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
    { slug: 'environments', path: `/projects/${projectId}/environments`, waitFor: /Environments/i },
    { slug: 'repositories', path: `/projects/${projectId}/repositories`, waitFor: /Repositories/i },
  ]

  for (const target of pages) {
    for (const viewport of viewports) {
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
