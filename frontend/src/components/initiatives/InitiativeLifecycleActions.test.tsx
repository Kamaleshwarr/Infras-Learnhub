import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { initiativesApi } from '../../api/initiativesApi'
import type { Initiative } from '../../types/initiatives'
import { getAvailableLifecycleActions, InitiativeLifecycleActions } from './InitiativeLifecycleActions'

vi.mock('../../api/initiativesApi', () => ({
  initiativesApi: {
    publish: vi.fn(),
    returnToDraft: vi.fn(),
    markExpired: vi.fn(),
    reactivate: vi.fn(),
  },
}))

const baseInitiative: Initiative = {
  createdAtUtc: '2026-01-01T00:00:00Z',
  description: 'Program details',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: 'initiative-1',
  startDateUtc: '2026-06-01T00:00:00Z',
  status: 'DRAFT',
  title: 'AWS Certification',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('getAvailableLifecycleActions', () => {
  it('returns publish for draft initiatives', () => {
    expect(getAvailableLifecycleActions('DRAFT')).toEqual(['publish'])
  })

  it('returns return to draft and mark expired for active initiatives', () => {
    expect(getAvailableLifecycleActions('ACTIVE')).toEqual(['returnToDraft', 'markExpired'])
  })

  it('returns reactivate for expired initiatives', () => {
    expect(getAvailableLifecycleActions('EXPIRED')).toEqual(['reactivate'])
  })
})

describe('InitiativeLifecycleActions', () => {
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
    vi.setSystemTime(new Date('2026-06-19T12:00:00.000Z'))
  })

  it('shows publish action for draft initiatives', () => {
    render(<InitiativeLifecycleActions initiative={baseInitiative} layout="buttons" onSuccess={onSuccess} />)

    expect(screen.getByRole('button', { name: 'Publish' })).toBeInTheDocument()
  })

  it('confirms publish and calls API', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.publish).mockResolvedValue({ ...baseInitiative, status: 'ACTIVE' })

    render(<InitiativeLifecycleActions initiative={baseInitiative} layout="buttons" onSuccess={onSuccess} />)

    await user.click(screen.getByRole('button', { name: 'Publish' }))
    expect(screen.getByRole('dialog', { name: 'Publish initiative' })).toBeInTheDocument()
    expect(screen.getByText('AWS Certification')).toBeInTheDocument()
    expect(screen.getByText(/Employees will gain access when the configured start date is reached/i)).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Publish' }))

    expect(initiativesApi.publish).toHaveBeenCalledWith('initiative-1')
    expect(onSuccess).toHaveBeenCalledWith('publish', expect.objectContaining({ status: 'ACTIVE' }))
  })

  it('shows return to draft and mark expired for active initiatives', () => {
    render(
      <InitiativeLifecycleActions
        initiative={{ ...baseInitiative, status: 'ACTIVE' }}
        layout="buttons"
        onSuccess={onSuccess}
      />,
    )

    expect(screen.getByRole('button', { name: 'Return to Draft' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Mark as Expired' })).toBeInTheDocument()
  })

  it('confirms return to draft with informative copy', async () => {
    const user = userEvent.setup({ delay: null })
    vi.mocked(initiativesApi.returnToDraft).mockResolvedValue({ ...baseInitiative, status: 'DRAFT' })

    render(
      <InitiativeLifecycleActions
        initiative={{ ...baseInitiative, status: 'ACTIVE' }}
        layout="buttons"
        onSuccess={onSuccess}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Return to Draft' }))
    expect(screen.getByText(/no new certificate submissions will be accepted/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Return to Draft' }))

    expect(initiativesApi.returnToDraft).toHaveBeenCalledWith('initiative-1')
    expect(onSuccess).toHaveBeenCalledWith('returnToDraft', expect.objectContaining({ status: 'DRAFT' }))
  })

  it('rejects reactivate expiry dates before today', async () => {
    const user = userEvent.setup({ delay: null })

    render(
      <InitiativeLifecycleActions
        initiative={{ ...baseInitiative, status: 'EXPIRED', expiryDateUtc: '2026-06-01T00:00:00Z' }}
        layout="buttons"
        onSuccess={onSuccess}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Reactivate' }))
    await user.click(screen.getByRole('button', { name: 'Reactivate' }))

    expect(screen.getByText(/Expiry date cannot be earlier than today/i)).toBeInTheDocument()
    expect(initiativesApi.reactivate).not.toHaveBeenCalled()
  })
})
