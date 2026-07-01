import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { useAuth } from '../../auth/useAuth'
import { InitiativeListPage } from './InitiativeListPage'
import { InitiativeDetailPage } from './InitiativeDetailPage'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    create: vi.fn(),
    get: vi.fn(),
    list: vi.fn(),
  },
}))

vi.mock('../../api/leaderboardsApi', () => ({
  leaderboardsApi: {
    initiative: vi.fn().mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    }),
  },
}))

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    listMine: vi.fn().mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    }),
  },
}))

vi.mock('../../auth/useAuth', () => ({
  useAuth: vi.fn(),
}))

const initiative = {
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  rewardDescription: 'Learning credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE' as const,
  title: 'AWS Certification',
}

const pageResponse = {
  content: [initiative],
  first: true,
  last: true,
  page: 0,
  size: 20,
  sort: [],
  totalElements: 1,
  totalPages: 1,
}

function renderListPage(initialEntry = '/initiatives') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<InitiativeListPage />} path="/initiatives" />
        <Route element={<InitiativeDetailPage />} path="/initiatives/:initiativeId" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('InitiativeListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'EMPLOYEE',
      hasRole: (role) => role === 'EMPLOYEE',
      isAdmin: false,
      isAuthenticated: true,
      isEmployee: true,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })
    vi.mocked(initiativesApi.list).mockResolvedValue(pageResponse)
    vi.mocked(initiativesApi.get).mockResolvedValue(initiative)
  })

  it('renders initiatives for employees', async () => {
    renderListPage()

    expect(screen.getByRole('heading', { name: 'Learning Initiatives' })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    expect(initiativesApi.list).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'expiryDateUtc,asc',
    })
  })

  it('opens create dialog from admin toolbar and refreshes list after success', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'ADMIN',
      hasRole: (role) => role === 'ADMIN',
      isAdmin: true,
      isAuthenticated: true,
      isEmployee: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })
    vi.mocked(initiativesApi.create).mockResolvedValue({
      ...initiative,
      id: 'initiative-new',
      title: 'New Initiative',
    })
    vi.mocked(initiativesApi.list)
      .mockResolvedValueOnce(pageResponse)
      .mockResolvedValueOnce({
        ...pageResponse,
        content: [{ ...initiative, id: 'initiative-new', title: 'New Initiative' }],
        totalElements: 2,
      })

    renderListPage()

    await user.click(await screen.findByRole('button', { name: 'Create Initiative' }))
    expect(screen.getByRole('dialog', { name: 'Create Initiative' })).toBeInTheDocument()

    const dialog = within(screen.getByRole('dialog', { name: 'Create Initiative' }))
    await user.type(dialog.getByLabelText(/^Title/i), 'New Initiative')
    await user.type(dialog.getByLabelText(/^Description/i), 'New certification program')
    await user.click(dialog.getByRole('button', { name: 'Create' }))

    await waitFor(() => expect(initiativesApi.create).toHaveBeenCalled())
    await waitFor(() => expect(screen.getByText('Initiative created.')).toBeInTheDocument())
    await waitFor(() => expect(initiativesApi.list).toHaveBeenCalledTimes(2))
    await waitFor(() =>
      expect(screen.queryByRole('dialog', { name: 'Create Initiative' })).not.toBeInTheDocument(),
    )
  })

  it('shows admin create toolbar', async () => {
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'ADMIN',
      hasRole: (role) => role === 'ADMIN',
      isAdmin: true,
      isAuthenticated: true,
      isEmployee: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })

    renderListPage()

    expect(await screen.findByRole('button', { name: 'Create Initiative' })).toBeInTheDocument()
  })

  it('does not show admin create toolbar for employees', async () => {
    renderListPage()

    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    expect(screen.queryByRole('button', { name: 'Create Initiative' })).not.toBeInTheDocument()
  })

  it('shows admin status filter and passes status to API', async () => {
    vi.mocked(useAuth).mockReturnValue({
      currentRole: 'ADMIN',
      hasRole: (role) => role === 'ADMIN',
      isAdmin: true,
      isAuthenticated: true,
      isEmployee: false,
      isLoading: false,
      login: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
      user: null,
    })

    renderListPage('/initiatives?status=DRAFT')

    await waitFor(() => expect(screen.getByText('Draft')).toBeInTheDocument())
    await waitFor(() =>
      expect(initiativesApi.list).toHaveBeenCalledWith({
        page: 0,
        size: 20,
        sort: 'expiryDateUtc,asc',
        status: 'DRAFT',
      }),
    )
  })

  it('shows empty state when no initiatives match search', async () => {
    vi.mocked(initiativesApi.list).mockResolvedValue({
      ...pageResponse,
      content: [],
      totalElements: 0,
    })

    renderListPage('/initiatives?search=missing')

    await waitFor(() =>
      expect(screen.getByText(/No initiatives match your search/i)).toBeInTheDocument(),
    )
    expect(screen.getByRole('button', { name: /Clear search/i })).toBeInTheDocument()
  })

  it('shows error state with retry', async () => {
    vi.mocked(initiativesApi.list).mockRejectedValue(new Error('network'))

    renderListPage()

    await waitFor(() =>
      expect(screen.getByText(/Unable to load initiatives/i)).toBeInTheDocument(),
    )
    expect(screen.getByRole('button', { name: /Retry/i })).toBeInTheDocument()
  })

  it('navigates to initiative detail on row click', async () => {
    const user = userEvent.setup()
    renderListPage()

    await waitFor(() => expect(screen.getByText('AWS Certification')).toBeInTheDocument())
    await user.click(screen.getByText('AWS Certification'))

    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'AWS Certification' })).toBeInTheDocument(),
    )
  })
})
