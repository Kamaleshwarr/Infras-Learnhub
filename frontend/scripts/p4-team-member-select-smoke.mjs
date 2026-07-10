import { chromium, request } from 'playwright'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'

async function loginToken(email, password) {
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

async function ensureTeamProject(adminToken) {
  const api = await request.newContext()
  const create = await api.post(`${apiUrl}/projects`, {
    data: {
      name: `P4 Select Smoke ${Date.now()}`,
      description: 'Team member select regression',
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

async function searchCandidateUser(adminToken) {
  const api = await request.newContext()
  const users = await api.get(`${apiUrl}/users?size=50&page=0`, {
    headers: { Authorization: `Bearer ${adminToken}` },
  })
  const body = await users.json()
  await api.dispose()
  const employee = body.content.find((user) => user.email === 'employee@learninghub.local')
  if (!employee) {
    throw new Error('employee@learninghub.local not found for add-member flow')
  }
  return employee
}

async function readComboboxValue(page, labelId) {
  return page.evaluate((id) => {
    const trigger = document.querySelector(`[role="combobox"][aria-labelledby="${id}"]`)
    return trigger?.textContent?.trim() ?? ''
  }, labelId)
}

function comboboxByLabel(page, labelId) {
  return page.locator(`[role="combobox"][aria-labelledby="${labelId}"]`)
}

/**
 * Simulates a real opening click: press on trigger, hold, release at the same coordinates.
 * After the menu opens, those coordinates may sit over a menu item — the production bug path.
 */
async function openSelectWithPointerHold(page, labelId) {
  const trigger = comboboxByLabel(page, labelId)
  await trigger.waitFor({ state: 'visible' })
  const before = await readComboboxValue(page, labelId)
  const box = await trigger.boundingBox()
  if (!box) {
    throw new Error(`No bounding box for combobox: ${labelId}`)
  }
  const x = box.x + box.width / 2
  const y = box.y + box.height / 2
  await page.mouse.move(x, y)
  await page.mouse.down()
  await page.waitForTimeout(280)
  const listboxVisible = await page
    .getByRole('listbox')
    .isVisible()
    .catch(() => false)
  await page.mouse.up()
  await page.waitForTimeout(50)
  const after = await readComboboxValue(page, labelId)
  if (await page.getByRole('listbox').isVisible().catch(() => false)) {
    await page.keyboard.press('Escape')
    await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 }).catch(() => {})
  }
  return { before, after, listboxVisible, trigger }
}

async function smoke() {
  const adminToken = await loginToken('admin@learninghub.local', 'Admin@12345')
  const projectId = await ensureTeamProject(adminToken)
  const candidateUser = await searchCandidateUser(adminToken)

  const browser = await chromium.launch()
  const page = await browser.newPage()
  await page.route('**/api/v1/**', async (route) => {
    const upstream = route.request().url().replace(`${appUrl}/api/v1`, apiUrl)
    await route.fulfill({ response: await route.fetch({ url: upstream }) })
  })
  await page.addInitScript((token) => {
    window.sessionStorage.setItem('elh.accessToken', token)
  }, adminToken)

  await page.goto(`${appUrl}/projects/${projectId}/team`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: 'Team & Contacts' }).waitFor()
  await page.getByRole('button', { name: /Add member/i }).click()
  await page.getByRole('dialog').waitFor()

  // Populate user candidates, then verify pointer-open does not auto-select
  await page.getByLabel(/search users/i).fill(candidateUser.fullName.split(' ')[0])
  await page.waitForTimeout(400)
  const userPointer = await openSelectWithPointerHold(page, 'team-member-user-label')
  if (userPointer.before !== userPointer.after) {
    throw new Error(`User select changed on open: "${userPointer.before}" -> "${userPointer.after}"`)
  }
  console.log('ADD user select preserved on pointer-open:', userPointer.after || '(empty)')

  await comboboxByLabel(page, 'team-member-user-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await page
    .getByRole('option', { name: new RegExp(candidateUser.fullName, 'i') })
    .click({ force: true })
  const selectedUser = await readComboboxValue(page, 'team-member-user-label')
  if (!selectedUser.includes(candidateUser.fullName)) {
    throw new Error(`User explicit selection failed, got: "${selectedUser}"`)
  }

  const addFunctional = await openSelectWithPointerHold(page, 'team-functional-role-label')
  console.log('ADD functional role before:', addFunctional.before)
  console.log('ADD functional role after:', addFunctional.after)
  console.log('ADD listbox still visible:', addFunctional.listboxVisible)

  if (addFunctional.before !== addFunctional.after) {
    throw new Error(
      `Add Member functional role changed on open: "${addFunctional.before}" -> "${addFunctional.after}"`,
    )
  }

  // Explicit selection must still work
  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await page.getByRole('option', { name: 'QA Lead' }).click({ force: true })
  const explicitValue = await readComboboxValue(page, 'team-functional-role-label')
  if (!explicitValue.includes('QA Lead')) {
    throw new Error(`Explicit QA Lead selection failed, got: "${explicitValue}"`)
  }

  // Access role pointer-open check
  const access = await openSelectWithPointerHold(page, 'team-access-role-label')
  if (access.before !== access.after) {
    throw new Error(`Access role changed on open: "${access.before}" -> "${access.after}"`)
  }

  await page.getByRole('button', { name: 'Cancel' }).click()

  // Edit flow: add member through API then edit
  const api = await request.newContext()
  await api.post(`${apiUrl}/projects/${projectId}/members`, {
    data: {
      userId: candidateUser.id,
      projectRole: 'CONTRIBUTOR',
      functionalRole: 'TECH_LEAD',
      responsibility: 'Architecture',
      primaryContact: false,
    },
    headers: {
      Authorization: `Bearer ${adminToken}`,
      'Content-Type': 'application/json',
    },
  })
  await api.dispose()

  await page.reload({ waitUntil: 'networkidle' })
  await page.getByRole('button', { name: new RegExp(`Edit ${candidateUser.fullName}`, 'i') }).click()
  await page.getByRole('dialog').waitFor()

  const editFunctional = await openSelectWithPointerHold(page, 'team-functional-role-label')
  console.log('EDIT functional role before:', editFunctional.before)
  console.log('EDIT functional role after:', editFunctional.after)
  if (editFunctional.before !== editFunctional.after) {
    throw new Error(
      `Edit Member functional role changed on open: "${editFunctional.before}" -> "${editFunctional.after}"`,
    )
  }

  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('option', { name: 'Developer' }).click({ force: true })
  const editValue = await readComboboxValue(page, 'team-functional-role-label')
  if (!editValue.includes('Developer')) {
    throw new Error(`Edit explicit Developer selection failed, got: "${editValue}"`)
  }

  console.log('p4_team_member_select_smoke_ok')
  await browser.close()
}

smoke().catch((error) => {
  console.error(error)
  process.exit(1)
})
