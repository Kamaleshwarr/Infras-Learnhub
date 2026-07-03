import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { StageResourceManageDialog } from './StageResourceManageDialog'
import type { StageResourceAdminView } from '../../types/resourceOverride'

vi.mock('../../api/learnApi', () => ({
  learnApi: {
    getStageResources: vi.fn(),
    createResourceOverride: vi.fn(),
    updateResourceOverride: vi.fn(),
    restoreResourceDefault: vi.fn(),
    deleteResourceOverride: vi.fn(),
  },
}))

import { learnApi } from '../../api/learnApi'

const adminView: StageResourceAdminView = {
  stageSlug: 'introduction',
  stageTitle: 'Introduction',
  stageOrder: 1,
  resources: [
    {
      catalog: {
        slug: 'oracle-docs',
        title: 'Oracle Java Tutorial',
        url: 'https://docs.oracle.com/javase/tutorial/',
        type: 'OFFICIAL_DOCUMENTATION',
        provider: 'Oracle Docs',
        freePaid: 'FREE',
      },
      effective: {
        slug: 'oracle-docs',
        title: 'Oracle Java Tutorial',
        url: 'https://docs.oracle.com/javase/tutorial/',
        type: 'OFFICIAL_DOCUMENTATION',
        provider: 'Oracle Docs',
        freePaid: 'FREE',
      },
      override: null,
      overrideStatus: 'DEFAULT',
    },
  ],
}

describe('StageResourceManageDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('loads and displays catalog and effective resources', async () => {
    vi.mocked(learnApi.getStageResources).mockResolvedValue(adminView)

    render(
      <StageResourceManageDialog
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        stageSlug="introduction"
        stageTitle="Introduction"
        technologyId="11111111-1111-1111-1111-111111111111"
      />,
    )

    expect(await screen.findByRole('button', { name: 'Replace URL' })).toBeInTheDocument()
    expect(screen.getAllByText('Oracle Java Tutorial')).toHaveLength(2)
    expect(screen.getByText('Catalog default')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Disable' })).toBeInTheDocument()
  })

  it('creates a URL override from the replace form', async () => {
    const user = userEvent.setup()
    vi.mocked(learnApi.getStageResources).mockResolvedValue(adminView)
    vi.mocked(learnApi.createResourceOverride).mockResolvedValue({
      id: 'override-1',
      technologySlug: 'java',
      stageSlug: 'introduction',
      resourceSlug: 'oracle-docs',
      catalogResourceSlug: 'oracle-docs',
      resourceKind: 'LEARNING',
      disabled: false,
      overrideUrl: 'https://internal.example.com/java',
      preferred: false,
      enabled: true,
      reason: null,
      title: null,
      resourceType: null,
      provider: null,
      freePaid: null,
      resourceOrder: 0,
      organizationResource: false,
      status: 'URL_OVERRIDE',
    })
    vi.mocked(learnApi.getStageResources).mockResolvedValueOnce(adminView).mockResolvedValueOnce({
      ...adminView,
      resources: [
        {
          ...adminView.resources[0],
          effective: {
            ...adminView.resources[0].effective!,
            url: 'https://internal.example.com/java',
          },
          overrideStatus: 'URL_OVERRIDE',
        },
      ],
    })

    const onSuccess = vi.fn()
    render(
      <StageResourceManageDialog
        onClose={vi.fn()}
        onSuccess={onSuccess}
        open
        stageSlug="introduction"
        stageTitle="Introduction"
        technologyId="11111111-1111-1111-1111-111111111111"
      />,
    )

    await user.click(await screen.findByRole('button', { name: 'Replace URL' }))
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Save override' })).toBeInTheDocument()
    })
    const urlField = screen.getByRole('textbox', { name: /override url/i })
    await user.clear(urlField)
    await user.type(urlField, 'https://internal.example.com/java')
    await user.click(screen.getByRole('button', { name: 'Save override' }))

    expect(learnApi.createResourceOverride).toHaveBeenCalledWith(
      '11111111-1111-1111-1111-111111111111',
      expect.objectContaining({
        catalogResourceSlug: 'oracle-docs',
        overrideUrl: 'https://internal.example.com/java',
      }),
    )
    expect(onSuccess).toHaveBeenCalled()
  })

  it('restores catalog default', async () => {
    const user = userEvent.setup()
    const overriddenView: StageResourceAdminView = {
      ...adminView,
      resources: [
        {
          ...adminView.resources[0],
          effective: {
            ...adminView.resources[0].effective!,
            url: 'https://internal.example.com/java',
          },
          override: {
            id: 'override-1',
            technologySlug: 'java',
            stageSlug: 'introduction',
            resourceSlug: 'oracle-docs',
            catalogResourceSlug: 'oracle-docs',
            resourceKind: 'LEARNING',
            disabled: false,
            overrideUrl: 'https://internal.example.com/java',
            preferred: false,
            enabled: true,
            reason: null,
            title: null,
            resourceType: null,
            provider: null,
            freePaid: null,
            resourceOrder: 0,
            organizationResource: false,
            status: 'URL_OVERRIDE',
          },
          overrideStatus: 'URL_OVERRIDE',
        },
      ],
    }
    vi.mocked(learnApi.getStageResources).mockResolvedValue(overriddenView)
    vi.mocked(learnApi.restoreResourceDefault).mockResolvedValue(undefined)

    render(
      <StageResourceManageDialog
        onClose={vi.fn()}
        onSuccess={vi.fn()}
        open
        stageSlug="introduction"
        stageTitle="Introduction"
        technologyId="11111111-1111-1111-1111-111111111111"
      />,
    )

    await screen.findByRole('button', { name: 'Replace URL' })
    const restoreButton = await screen.findByRole('button', { name: 'Restore default' })
    await user.click(restoreButton)

    expect(learnApi.restoreResourceDefault).toHaveBeenCalledWith(
      '11111111-1111-1111-1111-111111111111',
      'introduction',
      'oracle-docs',
    )
  })
})
