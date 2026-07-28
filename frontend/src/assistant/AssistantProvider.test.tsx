import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AssistantProvider } from './AssistantProvider'
import { useAssistant } from './useAssistant'
import { assistantApi } from '../api/assistantApi'

vi.mock('../api/assistantApi', () => ({
  assistantApi: {
    getStatus: vi.fn(),
  },
}))

function StatusProbe() {
  const { enabled, loading, error } = useAssistant()
  return (
    <div>
      <div data-testid="loading">{String(loading)}</div>
      <div data-testid="enabled">{String(enabled)}</div>
      <div data-testid="error">{error ?? 'none'}</div>
    </div>
  )
}

describe('AssistantProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('marks assistant as enabled when status endpoint reports enabled=true', async () => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: true,
      llmProvider: 'mock',
      llmHealthy: true,
    })

    render(
      <AssistantProvider>
        <StatusProbe />
      </AssistantProvider>,
    )

    await waitFor(() => {
      expect(screen.getByTestId('enabled')).toHaveTextContent('true')
    })
    expect(screen.getByTestId('loading')).toHaveTextContent('false')
    expect(screen.getByTestId('error')).toHaveTextContent('none')
  })

  it('keeps assistant disabled when status endpoint reports enabled=false', async () => {
    vi.mocked(assistantApi.getStatus).mockResolvedValue({
      enabled: false,
      llmProvider: 'mock',
      llmHealthy: true,
    })

    render(
      <AssistantProvider>
        <StatusProbe />
      </AssistantProvider>,
    )

    await waitFor(() => {
      expect(screen.getByTestId('enabled')).toHaveTextContent('false')
    })
  })

  it('keeps assistant disabled when status loading fails', async () => {
    vi.mocked(assistantApi.getStatus).mockRejectedValue(new Error('network error'))

    render(
      <AssistantProvider>
        <StatusProbe />
      </AssistantProvider>,
    )

    await waitFor(() => {
      expect(screen.getByTestId('enabled')).toHaveTextContent('false')
    })
    expect(screen.getByTestId('error')).toHaveTextContent('Unable to load assistant status.')
  })
})
