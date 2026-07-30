import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { DashboardWelcomeBanner } from './DashboardWelcomeBanner'

vi.mock('../../auth/useAuth', () => ({
  useAuth: () => ({
    user: { fullName: 'Jane Doe' },
  }),
}))

describe('DashboardWelcomeBanner', () => {
  it('shows personalized greeting and dashboard title for employees', () => {
    render(
      <MemoryRouter>
        <DashboardWelcomeBanner isAdmin={false} />
      </MemoryRouter>,
    )

    expect(screen.getByText('Welcome back, Jane Doe')).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 1, name: 'Employee Dashboard' })).toBeInTheDocument()
    expect(screen.getByText('Your learning activity and resources.')).toBeInTheDocument()
  })

  it('shows admin dashboard title for administrators', () => {
    render(
      <MemoryRouter>
        <DashboardWelcomeBanner isAdmin />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { level: 1, name: 'Admin Dashboard' })).toBeInTheDocument()
  })
})
