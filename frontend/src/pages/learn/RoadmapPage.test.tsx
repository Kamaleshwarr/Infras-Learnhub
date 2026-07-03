import axios from 'axios'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { RoadmapPage } from './RoadmapPage'
import type { TechnologyProgress } from '../../types/progress'
import type { Roadmap } from '../../types/roadmap'

vi.mock('../../api/learnApi', () => ({
  learnApi: {
    getRoadmapByTechnologyId: vi.fn(),
    getTechnologyProgress: vi.fn(),
    enrollInTechnology: vi.fn(),
    completeStage: vi.fn(),
  },
}))

import { learnApi } from '../../api/learnApi'

const technologyId = '11111111-1111-1111-1111-111111111111'
const stageOneId = '22222222-2222-2222-2222-222222222222'
const stageTwoId = '33333333-3333-3333-3333-333333333333'
const enrollmentId = '44444444-4444-4444-4444-444444444444'

const roadmap: Roadmap = {
  technologyId,
  technologySlug: 'java',
  technologyName: 'Java',
  version: '1.0.0',
  description: 'Structured Java learning path.',
  source: 'platform-team',
  sourceUrl: 'https://roadmap.sh/java',
  catalogUpdatedAt: '2026-07-03T00:00:00Z',
  stageCount: 2,
  estimatedTotalEffort: '1 week + 2 weeks',
  recommendedStageOrder: 1,
  nextStageOrder: 2,
  stages: [
    {
      id: stageOneId,
      order: 1,
      slug: 'introduction',
      title: 'Introduction',
      description: 'Get started with Java.',
      estimatedEffort: '1 week',
      notes: null,
      learningResources: [
        {
          slug: 'oracle-docs',
          title: 'Oracle Java Tutorial',
          url: 'https://docs.oracle.com/javase/tutorial/',
          type: 'OFFICIAL_DOCUMENTATION',
          provider: 'Oracle Docs',
          freePaid: 'FREE',
        },
      ],
      practiceResources: [],
    },
    {
      id: stageTwoId,
      order: 2,
      slug: 'core-java',
      title: 'Core Java',
      description: 'Language fundamentals.',
      estimatedEffort: '2 weeks',
      notes: null,
      learningResources: [
        {
          slug: 'roadmap-sh',
          title: 'Java Roadmap',
          url: 'https://roadmap.sh/java',
          type: 'ARTICLE',
          provider: 'roadmap.sh',
          freePaid: 'FREE',
        },
      ],
      practiceResources: [],
    },
  ],
}

const progress: TechnologyProgress = {
  enrollmentId,
  technologyId,
  technologySlug: 'java',
  technologyName: 'Java',
  status: 'IN_PROGRESS',
  enrolledAt: '2026-07-03T00:00:00Z',
  startedAt: '2026-07-03T00:00:00Z',
  lastActivityAt: '2026-07-03T00:00:00Z',
  progressPercent: 0,
  totalStages: 2,
  completedStageCount: 0,
  currentStageId: stageOneId,
  currentStageOrder: 1,
  currentStageTitle: 'Introduction',
  nextStageId: stageTwoId,
  nextStageOrder: 2,
  nextStageTitle: 'Core Java',
  estimatedRemainingEffort: '1 week + 2 weeks',
  completedStageIds: [],
  completedStageOrders: [],
}

function mockNoProgress() {
  return Promise.reject(
    new axios.AxiosError('Not found', 'ERR_BAD_REQUEST', undefined, undefined, {
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: { headers: new axios.AxiosHeaders() },
      data: { message: 'Not found' },
    }),
  )
}

function renderRoadmap(path = `/learn/technologies/${technologyId}/roadmap`) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route element={<RoadmapPage />} path="/learn/technologies/:technologyId/roadmap" />
      </Routes>
    </MemoryRouter>,
  )
}

describe('RoadmapPage', () => {
  it('renders roadmap overview and ordered stages', async () => {
    vi.mocked(learnApi.getRoadmapByTechnologyId).mockResolvedValue(roadmap)
    vi.mocked(learnApi.getTechnologyProgress).mockImplementation(mockNoProgress)

    renderRoadmap()

    expect(await screen.findByRole('heading', { name: /Java — Learning Roadmap/i })).toBeInTheDocument()
    expect(screen.getByText('Structured Java learning path.')).toBeInTheDocument()
    expect(screen.getAllByText('Introduction').length).toBeGreaterThan(0)
    expect(screen.getAllByText('Core Java').length).toBeGreaterThan(0)
    expect(screen.getByRole('link', { name: /Oracle Java Tutorial/i })).toHaveAttribute(
      'href',
      'https://docs.oracle.com/javase/tutorial/',
    )
    expect(screen.getByRole('button', { name: 'Start Learning' })).toBeInTheDocument()
  })

  it('shows progress bar and complete stage action when enrolled', async () => {
    vi.mocked(learnApi.getRoadmapByTechnologyId).mockResolvedValue(roadmap)
    vi.mocked(learnApi.getTechnologyProgress).mockResolvedValue(progress)

    renderRoadmap()

    expect(await screen.findByText('Roadmap progress')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Complete Stage' })).toBeInTheDocument()
    expect(screen.getByText('Start here: Introduction')).toBeInTheDocument()
    expect(screen.getByText('Next recommended stage: Core Java')).toBeInTheDocument()
  })

  it('completes the current stage and refreshes progress', async () => {
    vi.mocked(learnApi.getRoadmapByTechnologyId).mockResolvedValue(roadmap)
    vi.mocked(learnApi.getTechnologyProgress)
      .mockResolvedValueOnce(progress)
      .mockResolvedValueOnce({
        ...progress,
        progressPercent: 50,
        completedStageCount: 1,
        completedStageIds: [stageOneId],
        completedStageOrders: [1],
        currentStageId: stageTwoId,
        currentStageOrder: 2,
        currentStageTitle: 'Core Java',
        nextStageId: null,
        nextStageOrder: null,
        nextStageTitle: null,
        estimatedRemainingEffort: '2 weeks',
      })
    vi.mocked(learnApi.completeStage).mockResolvedValue({
      id: enrollmentId,
      technologyId,
      technologySlug: 'java',
      technologyName: 'Java',
      status: 'IN_PROGRESS',
      enrolledAt: '2026-07-03T00:00:00Z',
      progressPercent: 50,
      currentStageOrder: 2,
      currentStageTitle: 'Core Java',
    })

    const user = userEvent.setup()
    renderRoadmap()

    await user.click(await screen.findByRole('button', { name: 'Complete Stage' }))

    expect(learnApi.completeStage).toHaveBeenCalledWith(enrollmentId, stageOneId)
    expect(await screen.findByText('Stage marked complete.')).toBeInTheDocument()
  })

  it('shows empty state when roadmap has no stages', async () => {
    vi.mocked(learnApi.getRoadmapByTechnologyId).mockResolvedValue({ ...roadmap, stages: [], stageCount: 0 })
    vi.mocked(learnApi.getTechnologyProgress).mockImplementation(mockNoProgress)

    renderRoadmap()

    expect(await screen.findByText('This roadmap has no stages yet.')).toBeInTheDocument()
  })

  it('shows not found state for missing roadmap', async () => {
    const axios = await import('axios')
    vi.mocked(learnApi.getRoadmapByTechnologyId).mockRejectedValue(
      new axios.AxiosError('Not found', 'ERR_BAD_REQUEST', undefined, undefined, {
        status: 404,
        statusText: 'Not Found',
        headers: {},
        config: { headers: new axios.AxiosHeaders() },
        data: { message: 'Not found' },
      }),
    )

    renderRoadmap()

    expect(await screen.findByText('No roadmap is available for this technology yet.')).toBeInTheDocument()
  })
})
