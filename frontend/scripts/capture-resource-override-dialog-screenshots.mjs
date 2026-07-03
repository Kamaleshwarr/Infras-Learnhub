import { chromium, request } from 'playwright'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'

const appUrl = process.env.APP_URL ?? 'http://localhost:5174'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const technologyId = process.env.TECH_ID ?? 'eb1559fa-b253-46f4-b7b3-9e7c1fa48ada'
const outputDir = process.argv[2] ?? '../docs/screenshots/learn-v11-resource-overrides-ui-polish/after'

async function fetchAccessToken() {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: {
      email: 'admin@learninghub.local',
      password: 'Admin@12345',
    },
    headers: { 'Content-Type': 'application/json' },
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
  const context = await browser.newContext({ viewport: { width: 1280, height: 900 } })
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
  await page.getByRole('heading', { name: /Learning Roadmap/i }).waitFor({ timeout: 20000 })
  await page.getByRole('button', { name: 'Manage Resources' }).first().click()
  await page.getByText('Manage stage resources').waitFor()
  await page.waitForTimeout(500)

  const dialogPath = path.join(outputDir, 'resource-override-dialog-polished.png')
  await page.getByRole('dialog', { name: /Manage stage resources/i }).screenshot({ path: dialogPath })
  console.log(`saved ${dialogPath}`)

  await browser.close()
}

capture().catch((error) => {
  console.error(error)
  process.exit(1)
})
