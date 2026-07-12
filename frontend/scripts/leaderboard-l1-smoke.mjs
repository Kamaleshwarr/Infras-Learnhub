import { chromium, request } from 'playwright'

const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const appUrl = process.env.APP_URL ?? 'http://localhost:5173'

async function login(email, password) {
  const api = await request.newContext()
  const response = await api.post(`${apiUrl}/auth/login`, {
    data: { email, password },
    headers: { 'Content-Type': 'application/json' },
  })
  if (!response.ok()) {
    throw new Error(`Login failed for ${email}: ${response.status()}`)
  }
  const body = await response.json()
  await api.dispose()
  return body.accessToken
}

async function getJson(token, path) {
  const api = await request.newContext()
  const response = await api.get(`${apiUrl}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  const body = await response.json()
  await api.dispose()
  return { status: response.status(), body }
}

async function smoke() {
  const employeeToken = await login('employee@learninghub.local', 'Employee@12345')
  const adminToken = await login('admin@learninghub.local', 'Admin@12345')

  const global = await getJson(employeeToken, '/leaderboards/global?size=5&sort=rank,asc')
  const me = await getJson(employeeToken, '/leaderboards/me')
  const initiatives = await getJson(employeeToken, '/initiatives?size=1&status=ACTIVE')

  console.log('api_global_status', global.status)
  console.log('api_me_status', me.status)
  console.log('api_initiatives_status', initiatives.status)

  if (global.status !== 200 || me.status !== 200) {
    throw new Error('Leaderboard API smoke failed')
  }

  const initiativeId = initiatives.body?.content?.[0]?.id
  if (initiativeId) {
    const initiativeLeaderboard = await getJson(employeeToken, `/leaderboards/initiatives/${initiativeId}?size=5`)
    console.log('api_initiative_leaderboard_status', initiativeLeaderboard.status)
    if (initiativeLeaderboard.status !== 200) {
      throw new Error('Initiative leaderboard API smoke failed')
    }
  }

  const draftProbe = await getJson(employeeToken, '/initiatives?size=50')
  const draftInitiative = (draftProbe.body?.content ?? []).find((item) => item.status === 'DRAFT')
  if (draftInitiative?.id) {
    const blocked = await getJson(employeeToken, `/leaderboards/initiatives/${draftInitiative.id}`)
    console.log('api_draft_initiative_visibility_status', blocked.status)
    if (blocked.status !== 404) {
      throw new Error('Expected employee draft initiative leaderboard to return 404')
    }
    const adminView = await getJson(adminToken, `/leaderboards/initiatives/${draftInitiative.id}`)
    console.log('api_admin_draft_initiative_status', adminView.status)
  }

  const browser = await chromium.launch()
  const page = await browser.newPage()
  await page.route('**/api/v1/**', async (route) => {
    const upstream = route.request().url().replace(`${appUrl}/api/v1`, apiUrl)
    await route.fulfill({ response: await route.fetch({ url: upstream }) })
  })
  await page.addInitScript((accessToken) => {
    window.sessionStorage.setItem('elh.accessToken', accessToken)
  }, employeeToken)

  await page.goto(`${appUrl}/leaderboards/global`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: 'Global Leaderboard' }).waitFor()
  await page.getByText('Top Performers').waitFor()
  await page.getByRole('tab', { name: 'Initiative' }).click()
  await page.getByText('Select an initiative to view its leaderboard.').waitFor()

  if (initiativeId) {
    await page.goto(`${appUrl}/leaderboards/initiatives/${initiativeId}`, { waitUntil: 'networkidle' })
    await page.getByText(/Ranking is based on certification submission time/i).waitFor()
    await page.goto(`${appUrl}/initiatives/${initiativeId}`, { waitUntil: 'networkidle' })
    await page.getByRole('link', { name: 'View Leaderboard' }).waitFor()
  }

  await page.goto(`${appUrl}/`, { waitUntil: 'networkidle' })
  await page.getByRole('link', { name: /View global leaderboard/i }).first().waitFor()

  console.log('smoke_ok', true)
  await browser.close()
}

smoke().catch((error) => {
  console.error(error)
  process.exit(1)
})
