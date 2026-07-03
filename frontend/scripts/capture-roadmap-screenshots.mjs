import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const technologyId = process.env.TECH_ID ?? 'eb1559fa-b253-46f4-b7b3-9e7c1fa48ada'
const outputDir = process.argv[2] ?? '../docs/screenshots/f18-roadmap-ui-polish/after'
const prefix = process.argv[3] ?? 'after'

const viewports = [
  { name: 'desktop', width: 1280, height: 900 },
  { name: 'tablet', width: 834, height: 1100 },
  { name: 'mobile', width: 390, height: 1200 },
]

async function fetchAccessToken() {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: {
      email: 'employee@learninghub.local',
      password: 'Employee@12345',
    },
    headers: {
      'Content-Type': 'application/json',
    },
  })
  if (!response.ok()) {
    throw new Error(`Login failed: ${response.status()} ${await response.text()}`)
  }
  const body = await response.json()
  await api.dispose()
  return body.accessToken
}

async function capture() {
  await mkdir(outputDir, { recursive: true })
  const accessToken = await fetchAccessToken()
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
  }, accessToken)

  await page.goto(`${appUrl}/learn/technologies/${technologyId}/roadmap`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: /Learning Roadmap/i }).waitFor({ timeout: 15000 })
  await page.waitForTimeout(800)

  for (const viewport of viewports) {
    await page.setViewportSize({ width: viewport.width, height: viewport.height })
    await page.waitForTimeout(400)
    const filePath = path.join(outputDir, `${prefix}-${viewport.name}.png`)
    await page.screenshot({ path: filePath, fullPage: true })
    console.log(`saved ${filePath}`)
  }

  await browser.close()
}

capture().catch((error) => {
  console.error(error)
  process.exit(1)
})
