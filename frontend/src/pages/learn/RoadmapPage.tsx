import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Snackbar, Stack, useMediaQuery, useTheme } from '@mui/material'
import { useParams } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { RoadmapHero } from '../../components/learn/RoadmapHero'
import { RoadmapJourneyTimeline } from '../../components/learn/RoadmapJourneyTimeline'
import { RoadmapStageCard } from '../../components/learn/RoadmapStageCard'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import type { TechnologyProgress } from '../../types/progress'
import type { Roadmap } from '../../types/roadmap'
import { isConflictError, isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function RoadmapPage() {
  const { technologyId } = useParams()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [roadmap, setRoadmap] = useState<Roadmap | null>(null)
  const [progress, setProgress] = useState<TechnologyProgress | null>(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [completingStageId, setCompletingStageId] = useState<string | null>(null)
  const [enrolling, setEnrolling] = useState(false)
  const [notification, setNotification] = useState<string | null>(null)

  const loadRoadmap = useCallback(async () => {
    if (!technologyId) {
      setNotFound(true)
      setLoading(false)
      return
    }

    setLoading(true)
    setError(null)
    setNotFound(false)

    try {
      const roadmapResponse = await learnApi.getRoadmapByTechnologyId(technologyId)
      setRoadmap(roadmapResponse)

      try {
        const progressResponse = await learnApi.getTechnologyProgress(technologyId)
        setProgress(progressResponse)
      } catch (progressError) {
        if (!isNotFoundError(progressError)) {
          throw progressError
        }
        setProgress(null)
      }
    } catch (loadError) {
      if (isNotFoundError(loadError)) {
        setNotFound(true)
      } else {
        setError(resolveApiError(loadError, LEARN_MESSAGES.roadmapLoadError))
      }
      setRoadmap(null)
      setProgress(null)
    } finally {
      setLoading(false)
    }
  }, [technologyId])

  useEffect(() => {
    void loadRoadmap()
  }, [loadRoadmap])

  const activeStep = useMemo(() => {
    if (!roadmap || !progress?.currentStageOrder) {
      return 0
    }
    const index = roadmap.stages.findIndex((stage) => stage.order === progress.currentStageOrder)
    return index >= 0 ? index : 0
  }, [progress?.currentStageOrder, roadmap])

  async function handleStartLearning() {
    if (!technologyId) {
      return
    }

    setEnrolling(true)
    try {
      await learnApi.enrollInTechnology(technologyId)
      setNotification(LEARN_MESSAGES.progressEnrollSuccess)
      await loadRoadmap()
    } catch (enrollError) {
      if (isConflictError(enrollError)) {
        setNotification(LEARN_MESSAGES.progressAlreadyEnrolled)
        await loadRoadmap()
      } else {
        setError(resolveApiError(enrollError, LEARN_MESSAGES.progressEnrollError))
      }
    } finally {
      setEnrolling(false)
    }
  }

  async function handleCompleteStage(stageId: string) {
    if (!progress?.enrollmentId) {
      return
    }

    setCompletingStageId(stageId)
    try {
      await learnApi.completeStage(progress.enrollmentId, stageId)
      setNotification(LEARN_MESSAGES.progressCompleteStageSuccess)
      await loadRoadmap()
    } catch (completeError) {
      setError(resolveApiError(completeError, LEARN_MESSAGES.progressCompleteStageError))
    } finally {
      setCompletingStageId(null)
    }
  }

  function handleContinueLearning() {
    const currentStage = roadmap?.stages[activeStep]
    if (!currentStage) {
      return
    }
    document.getElementById(`stage-${currentStage.slug}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  if (loading) {
    return <Alert severity="info">Loading roadmap...</Alert>
  }

  if (notFound) {
    return <Alert severity="warning">{LEARN_MESSAGES.roadmapNotFound}</Alert>
  }

  if (error || !roadmap) {
    return <Alert severity="error">{error ?? LEARN_MESSAGES.roadmapLoadError}</Alert>
  }

  if (roadmap.stages.length === 0) {
    return <Alert severity="info">{LEARN_MESSAGES.roadmapEmptyStages}</Alert>
  }

  const isRoadmapComplete = progress?.status === 'COMPLETED'
  const hasEnrollment = progress != null

  return (
    <>
      <Stack spacing={2}>
        <RoadmapHero
          enrolling={enrolling}
          isRoadmapComplete={isRoadmapComplete}
          onContinueLearning={handleContinueLearning}
          onStartLearning={() => void handleStartLearning()}
          progress={progress}
          roadmap={roadmap}
          technologyId={technologyId!}
        />

        {hasEnrollment && progress ? (
          <RoadmapJourneyTimeline
            activeStep={activeStep}
            isMobile={isMobile}
            isRoadmapComplete={isRoadmapComplete}
            progress={progress}
            stages={roadmap.stages}
          />
        ) : null}

        <Box component="section">
          {roadmap.stages.map((stage, index) => {
            const isCompleted = progress?.completedStageIds.includes(stage.id) ?? false
            const isCurrent = progress?.currentStageId === stage.id
            const isUpcoming = hasEnrollment && !isCompleted && !isCurrent
            const canComplete = hasEnrollment && isCurrent && !isCompleted && !isRoadmapComplete

            return (
              <RoadmapStageCard
                canComplete={canComplete}
                completing={completingStageId === stage.id}
                isCompleted={isCompleted}
                isCurrent={isCurrent}
                isNext={stage.order === progress?.nextStageOrder}
                isUpcoming={isUpcoming}
                key={stage.slug}
                onCompleteStage={() => void handleCompleteStage(stage.id)}
                stage={stage}
                stageNumber={index + 1}
                totalStages={roadmap.stageCount}
              />
            )
          })}
        </Box>
      </Stack>

      <Snackbar
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        autoHideDuration={4000}
        message={notification}
        onClose={() => setNotification(null)}
        open={Boolean(notification)}
      />
    </>
  )
}
