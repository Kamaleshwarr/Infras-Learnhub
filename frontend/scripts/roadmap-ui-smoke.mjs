import { chromium, request } from 'playwright'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const technologyId = process.env.TECH_ID ?? 'eb1559fa-b253-46f4-b7b3-9e7c1fa48ada'

async function loginToken() {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: { email: 'employee@learninghub.local', password: 'Employee@12345' },
    headers: { 'Content-Type': 'application/json' },
  })
  const body = await response.json()
  await api.dispose()
  return body.accessToken
}

async function getProgress(token) {
  const api = await request.newContext()
  const response = await api.get(`${apiUrl}/learn/progress/technologies/${technologyId}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  const body = await response.json()
  await api.dispose()
  return body
}

async function smoke() {
  const token = await loginToken()
  const before = await getProgress(token)
  console.log('before', before.progressPercent, before.currentStageTitle)

  const browser = await chromium.launch()
  const page = await browser.newPage()
  await page.route('**/api/v1/**', async (route) => {
    const upstream = route.request().url().replace(`${appUrl}/api/v1`, apiUrl)
    await route.fulfill({ response: await route.fetch({ url: upstream }) })
  })
  await page.addInitScript((accessToken) => {
    window.sessionStorage.setItem('elh.accessToken', accessToken)
  }, token)

  await page.goto(`${appUrl}/learn/technologies/${technologyId}/roadmap`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: /Learning Roadmap/i }).waitFor()
  await page.getByLabel('Roadmap progress').waitFor()
  await page.getByText(/\d+ of \d+ stages completed/).waitFor()

  const completeButton = page.getByRole('button', { name: 'Complete Stage' })
  if (await completeButton.isVisible()) {
    await completeButton.click()
    await page.getByText('Stage marked complete.').waitFor({ timeout: 10000 })
  }

  const after = await getProgress(token)
  console.log('after', after.progressPercent, after.currentStageTitle)
  console.log('smoke_ok', after.progressPercent >= before.progressPercent)

  await browser.close()
}

smoke().catch((error) => {
  console.error(error)
  process.exit(1)
})
