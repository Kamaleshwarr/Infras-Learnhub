import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const outputDir = process.argv[2] ?? '../docs/screenshots/p1-project-portal'
const prefix = process.argv[3] ?? 'p1'

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

async function ensureSampleProject(adminToken) {
  const api = await request.newContext()
  const list = await api.get(`${apiUrl}/projects?size=1`, {
    headers: { Authorization: `Bearer ${adminToken}` },
  })
  const listBody = await list.json()
  if (listBody.totalElements > 0) {
    await api.dispose()
    return listBody.content[0].id
  }

  const create = await api.post(`${apiUrl}/projects`, {
    data: {
      name: `P1 Screenshot Project ${Date.now()}`,
      description: 'Sample project for P1 portal screenshots',
      accessType: 'PUBLIC',
    },
    headers: {
      Authorization: `Bearer ${adminToken}`,
      'Content-Type': 'application/json',
    },
  })
  const project = await create.json()
  await api.dispose()
  return project.id
}

async function capture() {
  await mkdir(outputDir, { recursive: true })
  const adminToken = await fetchAccessToken('admin@learninghub.local', 'Admin@12345')
  const projectId = await ensureSampleProject(adminToken)
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
    { slug: 'projects-list', path: '/projects', waitFor: /Projects/i },
    { slug: 'project-overview', path: `/projects/${projectId}`, waitFor: /About this project/i },
  ]

  for (const target of pages) {
    await page.goto(`${appUrl}${target.path}`, { waitUntil: 'networkidle' })
    await page.getByText(target.waitFor).first().waitFor({ timeout: 15000 })
    await page.waitForTimeout(800)

    for (const viewport of viewports) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.waitForTimeout(400)
      const filePath = path.join(outputDir, `${prefix}-${target.slug}-${viewport.name}.png`)
      await page.screenshot({ path: filePath, fullPage: true })
      console.log(`saved ${filePath}`)
    }
  }

  await browser.close()
}

capture().catch((error) => {
  console.error(error)
  process.exit(1)
})
