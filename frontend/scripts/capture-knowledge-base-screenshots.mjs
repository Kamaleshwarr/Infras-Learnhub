import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const outputDir = process.argv[2] ?? '../docs/screenshots/p2-knowledge-base'

const viewports = [
  { name: 'desktop', width: 1280, height: 900 },
  { name: 'tablet', width: 834, height: 1100 },
  { name: 'mobile', width: 390, height: 1200 },
]

async function login(email, password) {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: { email, password },
    headers: { 'Content-Type': 'application/json' },
  })
  const body = await response.json()
  await api.dispose()
  return body.accessToken
}

async function ensureKnowledgeData(adminToken) {
  const api = await request.newContext({ extraHTTPHeaders: { Authorization: `Bearer ${adminToken}` } })
  const projects = await (await api.get(`${apiUrl}/projects?size=1`)).json()
  let projectId = projects.content?.[0]?.id
  if (!projectId) {
    const created = await (await api.post(`${apiUrl}/projects`, {
      data: { name: `P2 Screenshot Project ${Date.now()}`, description: 'KB screenshots', accessType: 'PUBLIC' },
    })).json()
    projectId = created.id
  }

  const folders = await (await api.get(`${apiUrl}/projects/${projectId}/folders?size=100`)).json()
  let requirementsId = folders.content.find((folder) => folder.name === 'Requirements')?.id
  if (!requirementsId) {
    const created = await (await api.post(`${apiUrl}/projects/${projectId}/folders`, {
      data: { name: 'Requirements', description: 'Business requirements' },
    })).json()
    requirementsId = created.id
  }

  let architectureId = folders.content.find((folder) => folder.name === 'Architecture')?.id
  if (!architectureId) {
    const created = await (await api.post(`${apiUrl}/projects/${projectId}/folders`, {
      data: { name: 'Architecture', description: 'Architecture references', parentId: requirementsId },
    })).json()
    architectureId = created.id
  }

  const items = await (await api.get(`${apiUrl}/projects/${projectId}/items?folderId=${architectureId}&sourceType=LINK`)).json()
  if (items.totalElements === 0) {
    await api.post(`${apiUrl}/projects/${projectId}/items/links`, {
      data: {
        folderId: architectureId,
        title: 'API Documentation',
        description: 'Swagger reference',
        category: 'KT_DOCUMENTS',
        externalUrl: 'https://example.com/api-docs',
      },
    })
  }

  await api.dispose()
  return { projectId, architectureId }
}

async function capture() {
  await mkdir(outputDir, { recursive: true })
  const adminToken = await login('admin@learninghub.local', 'Admin@12345')
  const { architectureId, projectId } = await ensureKnowledgeData(adminToken)
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

  const targets = [
    { slug: 'knowledge-home', path: `/projects/${projectId}/knowledge`, wait: /Knowledge Base/i },
    { slug: 'knowledge-folder', path: `/projects/${projectId}/knowledge/folders/${architectureId}`, wait: /Architecture/i },
  ]

  for (const target of targets) {
    await page.goto(`${appUrl}${target.path}`, { waitUntil: 'networkidle' })
    await page.getByText(target.wait).first().waitFor({ timeout: 15000 })
    await page.waitForTimeout(800)
    for (const viewport of viewports) {
      await page.setViewportSize({ width: viewport.width, height: viewport.height })
      await page.waitForTimeout(400)
      const filePath = path.join(outputDir, `p2-${target.slug}-${viewport.name}.png`)
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
