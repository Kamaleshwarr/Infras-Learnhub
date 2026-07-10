import { chromium, request } from 'playwright'

const appUrl = process.env.APP_URL ?? 'http://localhost:5173'
const apiUrl = process.env.API_URL ?? 'http://localhost:8080/api/v1'
const POINTER_HOLD_MS = Number(process.env.POINTER_HOLD_MS ?? 400)

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
 * Real pointer open: mousedown on trigger, hold, mouseup at same coordinates.
 * This is the production bug path when the menu renders under the pointer.
 */
async function openSelectWithPointerHold(page, labelId, holdMs = POINTER_HOLD_MS, { closeAfter = true } = {}) {
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
  await page.waitForTimeout(holdMs)
  const listboxVisibleDuringHold = await page
    .getByRole('listbox')
    .isVisible()
    .catch(() => false)
  await page.mouse.up()
  await page.waitForTimeout(50)
  const after = await readComboboxValue(page, labelId)
  const stillOpenAfterRelease =
    (await trigger.getAttribute('aria-expanded')) === 'true' ||
    (await page
      .getByRole('listbox')
      .isVisible()
      .catch(() => false))
  if (closeAfter && stillOpenAfterRelease) {
    await page.keyboard.press('Escape')
    await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 }).catch(() => {})
  }
  return {
    before,
    after,
    listboxVisibleDuringHold,
    stillOpenAfterRelease,
    trigger,
    x,
    y,
  }
}

async function assertLabelDoesNotOverlapPlaceholder(page) {
  const overlap = await page.evaluate(() => {
    const label = document.querySelector('#team-functional-role-label')
    const trigger = document.querySelector('[role="combobox"][aria-labelledby="team-functional-role-label"]')
    const display = trigger?.firstElementChild
    if (!label || !trigger || !display) {
      return { ok: false, reason: 'missing elements' }
    }
    const labelBox = label.getBoundingClientRect()
    const displayBox = display.getBoundingClientRect()
    const triggerBox = trigger.getBoundingClientRect()
    const labelInNotch = labelBox.top < triggerBox.top + 4
    const textSeparated = labelBox.bottom <= displayBox.top + 1
    return {
      ok: labelInNotch && textSeparated,
      labelInNotch,
      textSeparated,
      labelBottom: labelBox.bottom,
      displayTop: displayBox.top,
      displayText: display.textContent?.trim(),
    }
  })
  if (!overlap.ok) {
    throw new Error(`Functional role label overlaps placeholder: ${JSON.stringify(overlap)}`)
  }
  console.log('ADD functional role label/placeholder layout ok')
}

async function assertMenuAnchoredNearTrigger(page, labelId) {
  const result = await page.evaluate((id) => {
    const trigger = document.querySelector(`[role="combobox"][aria-labelledby="${id}"]`)
    const listboxId = trigger?.getAttribute('aria-controls')
    const listbox = listboxId ? document.getElementById(listboxId) : document.querySelector('[role="listbox"]')
    const menu = listbox?.closest('.MuiPaper-root')
    if (!trigger || !menu) {
      return { ok: false, reason: 'missing trigger or menu' }
    }
    const triggerBox = trigger.getBoundingClientRect()
    const menuBox = menu.getBoundingClientRect()
    const horizontalOverlap =
      menuBox.left <= triggerBox.right && menuBox.right >= triggerBox.left
    const alignedToTrigger =
      Math.abs(menuBox.left - triggerBox.left) < 4 && Math.abs(menuBox.width - triggerBox.width) < 4
    const detachedPanel =
      Math.abs(menuBox.top) < 4 && Math.abs(menuBox.left) < 4 && menuBox.height > 400
    return {
      ok: horizontalOverlap && alignedToTrigger && !detachedPanel,
      horizontalOverlap,
      alignedToTrigger,
      detachedPanel,
    }
  }, labelId)
  if (!result.ok) {
    throw new Error(`Menu is not anchored to trigger: ${JSON.stringify(result)}`)
  }
  console.log(`Menu anchored to ${labelId} with standard Select positioning`)
}

async function selectOption(page, optionName) {
  await page.getByRole('option', { name: optionName }).click()
  await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 }).catch(() => {})
}

async function smokeViewport(page, adminToken, projectId, candidateUser, viewport) {
  if (viewport) {
    await page.setViewportSize(viewport)
  }

  await page.goto(`${appUrl}/projects/${projectId}/team`, { waitUntil: 'networkidle' })
  await page.getByRole('heading', { name: 'Team & Contacts' }).waitFor()
  await page.getByRole('button', { name: /Add member/i }).click()
  await page.getByRole('dialog').waitFor()

  await assertLabelDoesNotOverlapPlaceholder(page)

  await page.getByLabel(/search users/i).fill(candidateUser.fullName.split(' ')[0])
  await page.waitForTimeout(400)

  const userPointer = await openSelectWithPointerHold(page, 'team-member-user-label')
  if (userPointer.before !== userPointer.after) {
    throw new Error(`User select changed on open: "${userPointer.before}" -> "${userPointer.after}"`)
  }
  console.log(`${viewport ? 'MOBILE ' : ''}ADD user select preserved on pointer-open`)

  await comboboxByLabel(page, 'team-member-user-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await selectOption(page, new RegExp(candidateUser.fullName, 'i'))
  await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 })
  if (!(await readComboboxValue(page, 'team-member-user-label')).includes(candidateUser.fullName)) {
    throw new Error('User explicit selection failed')
  }

  const addFunctional = await openSelectWithPointerHold(page, 'team-functional-role-label', POINTER_HOLD_MS, {
    closeAfter: false,
  })
  console.log(`${viewport ? 'MOBILE ' : ''}ADD functional role before:`, addFunctional.before)
  console.log(`${viewport ? 'MOBILE ' : ''}ADD functional role after:`, addFunctional.after)
  if (addFunctional.before !== addFunctional.after) {
    throw new Error(
      `Add Member functional role changed on open: "${addFunctional.before}" -> "${addFunctional.after}"`,
    )
  }
  if (!addFunctional.stillOpenAfterRelease) {
    throw new Error('Add Member functional role menu did not stay open after pointer release')
  }

  await assertMenuAnchoredNearTrigger(page, 'team-functional-role-label')
  await page.keyboard.press('Escape')
  await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 }).catch(() => {})

  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await selectOption(page, 'QA Engineer')
  if (!(await readComboboxValue(page, 'team-functional-role-label')).includes('QA Engineer')) {
    throw new Error('Explicit QA Engineer selection failed')
  }

  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await selectOption(page, 'Developer')
  if (!(await readComboboxValue(page, 'team-functional-role-label')).includes('Developer')) {
    throw new Error('Explicit Developer selection failed')
  }

  const access = await openSelectWithPointerHold(page, 'team-access-role-label')
  if (access.before !== access.after) {
    throw new Error(`Access role changed on open: "${access.before}" -> "${access.after}"`)
  }

  let keyboardValue = await readComboboxValue(page, 'team-functional-role-label')
  await comboboxByLabel(page, 'team-functional-role-label').focus()
  await page.keyboard.press('ArrowDown')
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await page.keyboard.press('ArrowDown')
  await page.keyboard.press('Enter')
  await page.getByRole('listbox').waitFor({ state: 'hidden', timeout: 3000 })
  keyboardValue = await readComboboxValue(page, 'team-functional-role-label')
  if (keyboardValue === 'Developer') {
    throw new Error('Keyboard arrow + Enter did not change the functional role')
  }
  console.log(`${viewport ? 'MOBILE ' : ''}Keyboard navigation selection ok (${keyboardValue})`)

  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await page.keyboard.press('Escape')
  await page.getByRole('listbox').waitFor({ state: 'hidden' })
  if ((await readComboboxValue(page, 'team-functional-role-label')) !== keyboardValue) {
    throw new Error('Escape changed functional role value')
  }

  await page.getByRole('button', { name: 'Cancel' }).click()

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
  console.log(`${viewport ? 'MOBILE ' : ''}EDIT functional role before:`, editFunctional.before)
  console.log(`${viewport ? 'MOBILE ' : ''}EDIT functional role after:`, editFunctional.after)
  if (editFunctional.before !== editFunctional.after) {
    throw new Error(
      `Edit Member functional role changed on open: "${editFunctional.before}" -> "${editFunctional.after}"`,
    )
  }

  await comboboxByLabel(page, 'team-functional-role-label').click()
  await page.getByRole('listbox').waitFor({ state: 'visible' })
  await selectOption(page, 'QA Lead')
  await page.getByRole('button', { name: 'Save' }).click()
  await page.getByRole('dialog').waitFor({ state: 'hidden' })

  await page.getByRole('button', { name: new RegExp(`Edit ${candidateUser.fullName}`, 'i') }).click()
  await page.getByRole('dialog').waitFor()
  const persisted = await readComboboxValue(page, 'team-functional-role-label')
  if (!persisted.includes('QA Lead')) {
    throw new Error(`Edit save persistence failed, got: "${persisted}"`)
  }
  console.log(`${viewport ? 'MOBILE ' : ''}EDIT QA Lead persisted after save/reopen`)
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

  await smokeViewport(page, adminToken, projectId, candidateUser)
  await smokeViewport(page, adminToken, projectId, candidateUser, { width: 390, height: 844 })

  console.log('p4_team_member_select_smoke_ok')
  await browser.close()
}

smoke().catch((error) => {
  console.error(error)
  process.exit(1)
})
