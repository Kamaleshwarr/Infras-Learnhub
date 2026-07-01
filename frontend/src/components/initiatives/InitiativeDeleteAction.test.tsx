import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import { submissionsApi } from '../../api/submissionsApi'
import type { Initiative } from '../../types/initiatives'
import { InitiativeDeleteAction } from './InitiativeDeleteAction'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    delete: vi.fn(),
  },
}))

vi.mock('../../api/submissionsApi', () => ({
  submissionsApi: {
    listAll: vi.fn(),
  },
}))

const initiative: Initiative = {
  description: 'Program details',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  startDateUtc: '2026-06-01T00:00:00Z',
  status: 'DRAFT',
  title: 'AWS Certification',
}

describe('InitiativeDeleteAction', () => {
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows confirmation dialog when no submissions exist', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })

    render(<InitiativeDeleteAction initiative={initiative} layout="button" onSuccess={onSuccess} />)

    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))

    expect(screen.getByRole('dialog', { name: 'Delete initiative' })).toBeInTheDocument()
    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText('Certificate submissions: 0')).toBeInTheDocument()
    expect(screen.getByText(/permanently deletes this initiative/i)).toBeInTheDocument()
  })

  it('deletes initiative after confirmation', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
    vi.mocked(initiativesApi.delete).mockResolvedValue()

    render(<InitiativeDeleteAction initiative={initiative} layout="button" onSuccess={onSuccess} />)

    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))
    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))

    await waitFor(() => expect(initiativesApi.delete).toHaveBeenCalledWith('initiative-1'))
    expect(onSuccess).toHaveBeenCalledTimes(1)
  })

  it('shows blocked informational dialog when submissions exist', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 2,
      totalPages: 2,
    })

    render(
      <InitiativeDeleteAction
        initiative={{ ...initiative, status: 'ACTIVE' }}
        layout="button"
        onSuccess={onSuccess}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))

    expect(screen.getByText('This initiative cannot be deleted.')).toBeInTheDocument()
    expect(screen.getByText(/Certificate submissions already exist/i)).toBeInTheDocument()
    expect(screen.getByText(/Return to Draft/i)).toBeInTheDocument()
    expect(screen.getByText(/Mark as Expired/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete initiative' })).not.toBeInTheDocument()
    expect(initiativesApi.delete).not.toHaveBeenCalled()
  })

  it('shows active warning in confirmation dialog', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })

    render(
      <InitiativeDeleteAction
        initiative={{ ...initiative, status: 'ACTIVE' }}
        layout="button"
        onSuccess={onSuccess}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))

    expect(screen.getByText(/immediately remove it from employee access/i)).toBeInTheDocument()
  })

  it('switches to blocked dialog when delete returns 409', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(submissionsApi.listAll).mockResolvedValue({
      content: [],
      first: true,
      last: true,
      page: 0,
      size: 1,
      sort: [],
      totalElements: 0,
      totalPages: 0,
    })
    vi.mocked(initiativesApi.delete).mockRejectedValue(
      new axios.AxiosError('Conflict', 'ERR_BAD_REQUEST', undefined, undefined, {
        data: { message: 'blocked' },
        headers: {},
        status: 409,
        statusText: 'Conflict',
        config: { headers: new axios.AxiosHeaders() },
      }),
    )

    render(<InitiativeDeleteAction initiative={initiative} layout="button" onSuccess={onSuccess} />)

    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))
    await user.click(screen.getByRole('button', { name: 'Delete initiative' }))

    expect(await screen.findByText('This initiative cannot be deleted.')).toBeInTheDocument()
    expect(onSuccess).not.toHaveBeenCalled()
  })
})
