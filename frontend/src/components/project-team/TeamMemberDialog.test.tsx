import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen, within, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { TeamMemberDialog } from './TeamMemberDialog'
import { projectsApi } from '../../api/projectsApi'
import { projectTeamApi } from '../../api/projectTeamApi'

vi.mock('../../api/projectsApi', () => ({
  projectsApi: {
    addOrUpdateMember: vi.fn(),
  },
}))

vi.mock('../../api/projectTeamApi', () => ({
  projectTeamApi: {
    searchMemberCandidates: vi.fn(),
  },
}))

const sampleMember = {
  id: 'member-1',
  projectId: 'project-1',
  projectRole: 'CONTRIBUTOR' as const,
  functionalRole: 'TECH_LEAD' as const,
  responsibility: 'Architecture reviews',
  primaryContact: true,
  displayOrder: 0,
  createdAtUtc: '2026-07-01T00:00:00Z',
  updatedAtUtc: '2026-07-01T00:00:00Z',
  user: { id: 'u1', employeeId: 'E1', fullName: 'Arun K', email: 'arun@example.com' },
}

describe('TeamMemberDialog functional role select', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(projectTeamApi.searchMemberCandidates).mockResolvedValue([
      { id: 'u2', employeeId: 'E2', fullName: 'Priya S', email: 'priya@example.com' },
    ])
    vi.mocked(projectsApi.addOrUpdateMember).mockResolvedValue(sampleMember)
  })

  it('does not change functional role when the menu is opened', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    const functionalRoleSelect = screen.getByRole('combobox', { name: /functional role/i })
    expect(functionalRoleSelect).toHaveTextContent('Select functional role')

    await user.click(functionalRoleSelect)
    expect(screen.getByRole('listbox')).toBeInTheDocument()
    expect(functionalRoleSelect).toHaveTextContent('Select functional role')
    expect(projectsApi.addOrUpdateMember).not.toHaveBeenCalled()
  })

  it('updates functional role only after an explicit option click', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    const functionalRoleSelect = screen.getByRole('combobox', { name: /functional role/i })
    await user.click(functionalRoleSelect)
    await user.click(screen.getByRole('option', { name: 'Developer' }))
    expect(functionalRoleSelect).toHaveTextContent('Developer')

    await user.click(functionalRoleSelect)
    await user.click(screen.getByRole('option', { name: 'QA Lead' }))
    expect(functionalRoleSelect).toHaveTextContent('QA Lead')
  })

  it('keeps access role independent from functional role changes', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    const accessRoleSelect = screen.getByRole('combobox', { name: /access/i })
    const functionalRoleSelect = screen.getByRole('combobox', { name: /functional role/i })

    await user.click(accessRoleSelect)
    await user.click(screen.getByRole('option', { name: 'Viewer' }))
    expect(accessRoleSelect).toHaveTextContent('Viewer')
    expect(functionalRoleSelect).toHaveTextContent('Select functional role')

    await user.click(functionalRoleSelect)
    await user.click(screen.getByRole('option', { name: 'Business Analyst' }))
    expect(accessRoleSelect).toHaveTextContent('Viewer')
    expect(functionalRoleSelect).toHaveTextContent('Business Analyst')
  })

  it('closes functional role menu on Escape without changing the value', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    const functionalRoleSelect = screen.getByRole('combobox', { name: /functional role/i })
    await user.click(functionalRoleSelect)
    expect(screen.getByRole('listbox')).toBeInTheDocument()
    await user.keyboard('{Escape}')
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument()
    expect(functionalRoleSelect).toHaveTextContent('Select functional role')
  })

  it('preserves edit member functional role until an explicit new choice', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={sampleMember}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    const functionalRoleSelect = screen.getByRole('combobox', { name: /functional role/i })
    expect(functionalRoleSelect).toHaveTextContent('Tech Lead')

    await user.click(functionalRoleSelect)
    expect(screen.getByRole('listbox')).toBeInTheDocument()
    expect(functionalRoleSelect).toHaveTextContent('Tech Lead')

    await user.click(screen.getByRole('option', { name: 'Developer' }))
    expect(functionalRoleSelect).toHaveTextContent('Developer')
  })

  it('submits updated functional role from edit dialog', async () => {
    const user = userEvent.setup()
    const onSuccess = vi.fn()
    render(
      <TeamMemberDialog
        member={sampleMember}
        onClose={vi.fn()}
        onSuccess={onSuccess}
        open
        projectId="project-1"
      />,
    )

    await user.click(screen.getByRole('combobox', { name: /functional role/i }))
    await user.click(screen.getByRole('option', { name: 'Developer' }))
    await user.click(screen.getByRole('button', { name: 'Save' }))

    expect(projectsApi.addOrUpdateMember).toHaveBeenCalledWith('project-1', {
      userId: 'u1',
      projectRole: 'CONTRIBUTOR',
      functionalRole: 'DEVELOPER',
      responsibility: 'Architecture reviews',
      primaryContact: true,
    })
    expect(onSuccess).toHaveBeenCalled()
  })

  it('requires functional role before save in add dialog', async () => {
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    expect(screen.getByRole('button', { name: 'Save' })).toBeDisabled()
  })

  it('does not change user select when opening the menu in add dialog', async () => {
    const user = userEvent.setup()
    render(
      <TeamMemberDialog
        member={null}
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        projectId="project-1"
      />,
    )

    await user.type(screen.getByLabelText(/search users/i), 'Priya')
    await waitFor(() => expect(projectTeamApi.searchMemberCandidates).toHaveBeenCalled())
    const userSelect = screen.getByRole('combobox', { name: /user/i })
    expect(userSelect).not.toHaveTextContent('Priya S')

    await user.click(userSelect)
    expect(screen.getByRole('listbox')).toBeInTheDocument()
    expect(userSelect).not.toHaveTextContent('Priya S')

    const listbox = screen.getByRole('listbox')
    await user.click(await within(listbox).findByRole('option', { name: /Priya S/i }))
    expect(userSelect).toHaveTextContent('Priya S (priya@example.com)')
  })
})
