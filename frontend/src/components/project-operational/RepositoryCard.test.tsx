import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { RepositoryCard } from './RepositoryCard'
import type { ProjectLinkedRepository } from '../../types/projectOperational'

const baseRepository: ProjectLinkedRepository = {
  id: 'repo-1',
  projectId: 'project-1',
  name: 'Backend Service',
  description: 'Spring Boot API',
  repositoryType: 'BACKEND',
  provider: 'GITHUB',
  repositoryUrl: 'https://github.com/example/backend',
  defaultBranch: 'main',
  displayOrder: 0,
  active: true,
  createdBy: { id: 'u1', employeeId: 'E1', fullName: 'Admin', email: 'admin@example.com' },
  createdAtUtc: '2026-07-01T00:00:00Z',
  updatedAtUtc: '2026-07-01T00:00:00Z',
}

function renderCard(overrides: Partial<ProjectLinkedRepository> = {}, props: { canManage?: boolean; canDelete?: boolean } = {}) {
  return render(
    <RepositoryCard
      canDelete={props.canDelete ?? false}
      canManage={props.canManage ?? false}
      onDelete={vi.fn()}
      onEdit={vi.fn()}
      repository={{ ...baseRepository, ...overrides }}
    />,
  )
}

describe('RepositoryCard', () => {
  it('renders description and default branch when present', () => {
    renderCard()

    expect(screen.getByText('Backend Service')).toBeInTheDocument()
    expect(screen.getByText('Spring Boot API')).toBeInTheDocument()
    expect(screen.getByText('Default branch: main')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Open Repository/i })).toHaveAttribute(
      'href',
      'https://github.com/example/backend',
    )
  })

  it('omits description and default branch when absent', () => {
    renderCard({ description: undefined, defaultBranch: undefined })

    expect(screen.getByText('Backend Service')).toBeInTheDocument()
    expect(screen.queryByText(/Default branch/i)).not.toBeInTheDocument()
    expect(screen.queryByText('Spring Boot API')).not.toBeInTheDocument()
    expect(screen.queryByText(/undefined|null/i)).not.toBeInTheDocument()
  })

  it('renders only description when branch is absent', () => {
    renderCard({ defaultBranch: undefined })

    expect(screen.getByText('Spring Boot API')).toBeInTheDocument()
    expect(screen.queryByText(/Default branch/i)).not.toBeInTheDocument()
  })

  it('renders only default branch when description is absent', () => {
    renderCard({ description: undefined })

    expect(screen.getByText('Default branch: main')).toBeInTheDocument()
    expect(screen.queryByText('Spring Boot API')).not.toBeInTheDocument()
  })

  it('wraps long repository names without showing placeholder values', () => {
    renderCard({
      name: 'Enterprise Payments Platform Backend Monorepo Service',
      description: undefined,
      defaultBranch: undefined,
    })

    expect(
      screen.getByText('Enterprise Payments Platform Backend Monorepo Service'),
    ).toBeInTheDocument()
  })

  it('shows management actions only when permitted', () => {
    const { rerender } = renderCard({}, { canManage: true, canDelete: false })
    expect(screen.getByRole('button', { name: /Edit/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Delete/i })).not.toBeInTheDocument()

    rerender(
      <RepositoryCard
        canDelete
        canManage
        onDelete={vi.fn()}
        onEdit={vi.fn()}
        repository={baseRepository}
      />,
    )
    expect(screen.getByRole('button', { name: /Delete/i })).toBeInTheDocument()
  })
})
