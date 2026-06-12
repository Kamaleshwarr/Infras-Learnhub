import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usersApi } from '../../api/usersApi'
import type { PageResponse } from '../../types/api'
import type { UserSummary } from '../../types/users'
import { UserListPage } from './UserListPage'

vi.mock('../../api/usersApi', () => ({
  usersApi: {
    list: vi.fn(),
  },
}))

const users: UserSummary[] = [
  {
    active: true,
    createdAtUtc: '2026-06-01T00:00:00Z',
    email: 'admin@example.com',
    employeeId: 'EMP001',
    fullName: 'Admin User',
    id: 'user-1',
    role: 'ADMIN',
    updatedAtUtc: '2026-06-01T00:00:00Z',
  },
  {
    active: false,
    createdAtUtc: '2026-06-02T00:00:00Z',
    email: 'employee@example.com',
    employeeId: 'EMP002',
    fullName: 'Employee User',
    id: 'user-2',
    role: 'EMPLOYEE',
    updatedAtUtc: '2026-06-02T00:00:00Z',
  },
]

const pageResponse: PageResponse<UserSummary> = {
  content: users,
  first: true,
  last: true,
  page: 0,
  size: 20,
  sort: [{ direction: 'ASC', property: 'employeeId' }],
  totalElements: 2,
  totalPages: 1,
}

function renderUserListPage(initialEntry = '/users') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route element={<UserListPage />} path="/users" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('UserListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(usersApi.list).mockResolvedValue(pageResponse)
  })

  it('renders users from the API', async () => {
    renderUserListPage()

    expect(screen.getByText('User Management')).toBeInTheDocument()

    await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument())
    expect(screen.getByText('Employee User')).toBeInTheDocument()
    expect(screen.getByText('EMP001')).toBeInTheDocument()
    expect(usersApi.list).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'employeeId,asc',
    })
  })

  it('loads list state from URL query parameters', async () => {
    renderUserListPage('/users?fullName=Jane&role=ADMIN&active=true&page=1&size=10&sort=email,desc')

    await waitFor(() =>
      expect(usersApi.list).toHaveBeenCalledWith({
        page: 1,
        size: 10,
        sort: 'email,desc',
        fullName: 'Jane',
        role: 'ADMIN',
        active: true,
      }),
    )
  })

  it('applies draft filters to the URL and reloads users', async () => {
    const user = userEvent.setup()
    renderUserListPage()

    await waitFor(() => expect(screen.getByText('Admin User')).toBeInTheDocument())

    await user.type(screen.getByLabelText('Employee ID'), 'EMP002')
    await user.click(screen.getByRole('button', { name: 'Apply filters' }))

    await waitFor(() =>
      expect(usersApi.list).toHaveBeenLastCalledWith({
        page: 0,
        size: 20,
        sort: 'employeeId,asc',
        employeeId: 'EMP002',
      }),
    )
  })

  it('shows must change password column when API returns the field', async () => {
    vi.mocked(usersApi.list).mockResolvedValue({
      ...pageResponse,
      content: [{ ...users[0], mustChangePassword: true }, users[1]],
    })

    renderUserListPage()

    expect(await screen.findByText('Must Change Password')).toBeInTheDocument()
    expect(screen.getByText('Yes')).toBeInTheDocument()
  })

  it('renders error state when loading fails', async () => {
    vi.mocked(usersApi.list).mockRejectedValue(new Error('network'))

    renderUserListPage()

    expect(
      await screen.findByText('Unable to load users. Please refresh or try again later.'),
    ).toBeInTheDocument()
  })
})
