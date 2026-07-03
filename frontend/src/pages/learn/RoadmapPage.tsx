import { useCallback, useEffect, useMemo, useState } from 'react'
import MapOutlinedIcon from '@mui/icons-material/MapOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  LinearProgress,
  Link,
  Snackbar,
  Stack,
  Step,
  StepLabel,
  Stepper,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { PageHeader } from '../../components/common/PageHeader'
import { RoadmapStageCard } from '../../components/learn/RoadmapStageCard'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import type { TechnologyProgress } from '../../types/progress'
import type { Roadmap } from '../../types/roadmap'
import { isConflictError, isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function RoadmapPage() {
  const { technologyId } = useParams()
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

  const remainingStages = useMemo(() => {
    if (!roadmap || !progress) {
      return roadmap?.stageCount ?? 0
    }
    return Math.max(roadmap.stageCount - progress.completedStageCount, 0)
  }, [progress, roadmap])

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

  if (loading) {
    return <Alert severity="info">Loading roadmap...</Alert>
  }

  if (notFound) {
    return (
      <Alert severity="warning">
        {LEARN_MESSAGES.roadmapNotFound}
        <Box sx={{ mt: 2 }}>
          <Button component={RouterLink} to={`/learn/technologies/${technologyId}`} variant="outlined">
            {LEARN_MESSAGES.roadmapBackToTechnology}
          </Button>
        </Box>
      </Alert>
    )
  }

  if (error || !roadmap) {
    return <Alert severity="error">{error ?? LEARN_MESSAGES.roadmapLoadError}</Alert>
  }

  if (roadmap.stages.length === 0) {
    return (
      <Alert severity="info">
        {LEARN_MESSAGES.roadmapEmptyStages}
        <Box sx={{ mt: 2 }}>
          <Button component={RouterLink} to={`/learn/technologies/${technologyId}`} variant="outlined">
            {LEARN_MESSAGES.roadmapBackToTechnology}
          </Button>
        </Box>
      </Alert>
    )
  }

  const currentStage = roadmap.stages[activeStep]
  const isRoadmapComplete = progress?.status === 'COMPLETED'
  const hasEnrollment = progress != null

  return (
    <>
      <PageHeader description={LEARN_MESSAGES.roadmapDescription} title={`${roadmap.technologyName} — ${LEARN_MESSAGES.roadmapTitle}`} />

      <Stack spacing={1} sx={{ mb: 3 }}>
        <Button component={RouterLink} to={`/learn/technologies/${technologyId}`} variant="text">
          {LEARN_MESSAGES.roadmapBackToTechnology}
        </Button>
        <Typography color="text.secondary" variant="body2">
          {LEARN_MESSAGES.roadmapWhereAmI} <strong>{roadmap.technologyName}</strong>.
        </Typography>
      </Stack>

      <Stack spacing={3}>
        {!hasEnrollment ? (
          <Alert
            action={
              <Button color="inherit" disabled={enrolling} onClick={() => void handleStartLearning()} size="small">
                {LEARN_MESSAGES.progressStartLearning}
              </Button>
            }
            severity="info"
          >
            {LEARN_MESSAGES.viewRoadmapHelper}
          </Alert>
        ) : null}

        <Card variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                <MapOutlinedIcon color="primary" />
                <Typography variant="h6">{LEARN_MESSAGES.roadmapSummaryTitle}</Typography>
              </Stack>
              {roadmap.description ? <Typography>{roadmap.description}</Typography> : null}
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                <Chip label={`${LEARN_MESSAGES.roadmapStageCount}: ${roadmap.stageCount}`} />
                <Chip label={`${LEARN_MESSAGES.roadmapEstimatedTotal}: ${roadmap.estimatedTotalEffort}`} />
                {hasEnrollment ? (
                  <Chip label={`${LEARN_MESSAGES.homeContinueLearningProgress}: ${progress.progressPercent}%`} color="primary" />
                ) : null}
                {hasEnrollment && progress.estimatedRemainingEffort ? (
                  <Chip
                    label={`${LEARN_MESSAGES.progressRemainingEffort}: ${progress.estimatedRemainingEffort}`}
                    variant="outlined"
                  />
                ) : null}
                <Chip label={`${LEARN_MESSAGES.roadmapVersion}: ${roadmap.version}`} />
                {roadmap.source ? (
                  <Chip label={`${LEARN_MESSAGES.roadmapCatalogSource}: ${roadmap.source}`} variant="outlined" />
                ) : null}
              </Stack>
              {hasEnrollment ? (
                <Box>
                  <Typography color="text.secondary" sx={{ mb: 0.5 }} variant="caption">
                    {LEARN_MESSAGES.progressBarLabel}
                  </Typography>
                  <LinearProgress aria-label={LEARN_MESSAGES.progressBarLabel} value={progress.progressPercent} variant="determinate" />
                </Box>
              ) : null}
              {isRoadmapComplete ? (
                <Alert severity="success">{LEARN_MESSAGES.progressEnrollmentComplete}</Alert>
              ) : null}
              <Typography color="text.secondary" variant="body2">
                {LEARN_MESSAGES.roadmapWhatIsThis}
              </Typography>
              {roadmap.sourceUrl ? (
                <Link href={roadmap.sourceUrl} rel="noopener noreferrer" target="_blank">
                  {roadmap.sourceUrl}
                </Link>
              ) : null}
            </Stack>
          </CardContent>
        </Card>

        {hasEnrollment ? (
          <Card variant="outlined">
            <CardContent>
              <Stack spacing={2}>
                <Typography variant="h6">{LEARN_MESSAGES.roadmapContinueLearning}</Typography>
                <Typography color="text.secondary" variant="body2">
                  {LEARN_MESSAGES.roadmapWhatNext}
                </Typography>
                <Stepper activeStep={activeStep} alternativeLabel nonLinear>
                  {roadmap.stages.map((stage) => {
                    const completed = progress.completedStageOrders.includes(stage.order)
                    return (
                      <Step completed={completed} key={stage.slug}>
                        <StepLabel
                          onClick={() => {
                            document.getElementById(`stage-${stage.slug}`)?.scrollIntoView({ behavior: 'smooth' })
                          }}
                          sx={{ cursor: 'pointer' }}
                        >
                          {stage.title}
                        </StepLabel>
                      </Step>
                    )
                  })}
                </Stepper>
                <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                  {!isRoadmapComplete && currentStage ? (
                    <Chip color="success" label={`${LEARN_MESSAGES.roadmapCurrentStage}: ${currentStage.title}`} />
                  ) : null}
                  {progress.nextStageTitle && !isRoadmapComplete ? (
                    <Chip
                      color="info"
                      label={`${LEARN_MESSAGES.progressNextRecommendedStage}: ${progress.nextStageTitle}`}
                      variant="outlined"
                    />
                  ) : null}
                  <Chip label={`${LEARN_MESSAGES.roadmapRemaining}: ${remainingStages}`} variant="outlined" />
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        ) : null}

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
