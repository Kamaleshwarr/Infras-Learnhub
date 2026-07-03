import { useCallback, useEffect, useMemo, useState } from 'react'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import MapOutlinedIcon from '@mui/icons-material/MapOutlined'
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  LinearProgress,
  Link,
  Snackbar,
  Stack,
  Step,
  StepConnector,
  stepConnectorClasses,
  StepLabel,
  Stepper,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material'
import type { StepIconProps } from '@mui/material/StepIcon'
import { styled } from '@mui/material/styles'
import { Link as RouterLink, useParams } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { PageHeader } from '../../components/common/PageHeader'
import { RoadmapStageCard } from '../../components/learn/RoadmapStageCard'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import type { TechnologyProgress } from '../../types/progress'
import type { Roadmap } from '../../types/roadmap'
import { isConflictError, isNotFoundError, resolveApiError } from '../../utils/apiErrors'

function RoadmapStat({ label, value }: { label: string; value: string | number }) {
  return (
    <Box
      sx={{
        bgcolor: 'background.default',
        border: 1,
        borderColor: 'divider',
        borderRadius: 1.5,
        minWidth: 0,
        p: 1.5,
      }}
    >
      <Typography color="text.secondary" sx={{ display: 'block', mb: 0.25 }} variant="caption">
        {label}
      </Typography>
      <Typography sx={{ fontWeight: 600, lineHeight: 1.3, wordBreak: 'break-word' }} variant="body2">
        {value}
      </Typography>
    </Box>
  )
}

const TimelineConnector = styled(StepConnector)(({ theme }) => ({
  [`&.${stepConnectorClasses.active}`]: {
    [`& .${stepConnectorClasses.line}`]: {
      borderColor: theme.palette.primary.main,
    },
  },
  [`&.${stepConnectorClasses.completed}`]: {
    [`& .${stepConnectorClasses.line}`]: {
      borderColor: theme.palette.success.main,
    },
  },
  [`& .${stepConnectorClasses.line}`]: {
    borderColor: theme.palette.divider,
    borderTopWidth: 3,
    borderRadius: 1,
  },
}))

function TimelineStepIcon({ active, completed }: StepIconProps) {
  if (completed) {
    return <CheckCircleIcon color="success" fontSize="small" />
  }
  if (active) {
    return (
      <Box
        sx={{
          alignItems: 'center',
          bgcolor: 'primary.main',
          borderRadius: '50%',
          color: 'primary.contrastText',
          display: 'flex',
          height: 24,
          justifyContent: 'center',
          width: 24,
        }}
      >
        <Box sx={{ bgcolor: 'primary.contrastText', borderRadius: '50%', height: 8, width: 8 }} />
      </Box>
    )
  }
  return <RadioButtonUncheckedIcon color="disabled" fontSize="small" />
}

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

      <Button
        component={RouterLink}
        size="small"
        sx={{ alignSelf: 'flex-start', mb: 2, ml: -0.5, px: 1 }}
        to={`/learn/technologies/${technologyId}`}
        variant="text"
      >
        {LEARN_MESSAGES.roadmapBackToTechnology}
      </Button>

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
          <CardContent sx={{ '&:last-child': { pb: { xs: 2, sm: 2.5 } }, p: { xs: 2, sm: 2.5 } }}>
            <Stack spacing={2.5}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                <MapOutlinedIcon color="primary" fontSize="small" />
                <Typography component="h2" sx={{ fontWeight: 700 }} variant="subtitle1">
                  {LEARN_MESSAGES.roadmapSummaryTitle}
                </Typography>
              </Stack>

              {roadmap.description ? (
                <Typography
                  color="text.secondary"
                  sx={{
                    WebkitBoxOrient: 'vertical',
                    WebkitLineClamp: 2,
                    display: '-webkit-box',
                    lineHeight: 1.5,
                    overflow: 'hidden',
                  }}
                  variant="body2"
                >
                  {roadmap.description}
                </Typography>
              ) : null}

              <Box
                sx={{
                  display: 'grid',
                  gap: 1.5,
                  gridTemplateColumns: {
                    xs: 'repeat(2, minmax(0, 1fr))',
                    md: hasEnrollment ? 'repeat(4, minmax(0, 1fr))' : 'repeat(3, minmax(0, 1fr))',
                  },
                }}
              >
                <RoadmapStat label={LEARN_MESSAGES.roadmapStageCount} value={roadmap.stageCount} />
                <RoadmapStat label={LEARN_MESSAGES.roadmapEstimatedTotal} value={roadmap.estimatedTotalEffort} />
                {hasEnrollment ? (
                  <RoadmapStat
                    label={LEARN_MESSAGES.progressRemainingEffort}
                    value={progress.estimatedRemainingEffort ?? `${remainingStages} stages`}
                  />
                ) : null}
                <RoadmapStat label={LEARN_MESSAGES.roadmapVersion} value={roadmap.version} />
              </Box>

              {hasEnrollment ? (
                <Box
                  sx={{
                    bgcolor: 'background.default',
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 2,
                    p: { xs: 2, sm: 2.5 },
                  }}
                >
                  <Stack
                    direction={{ sm: 'row' }}
                    spacing={1}
                    sx={{ alignItems: { sm: 'baseline' }, justifyContent: 'space-between', mb: 1.5 }}
                  >
                    <Typography sx={{ fontWeight: 700 }} variant="subtitle1">
                      {LEARN_MESSAGES.progressBarLabel}
                    </Typography>
                    <Typography color="primary.main" sx={{ fontWeight: 700 }} variant="h5">
                      {progress.progressPercent}%
                    </Typography>
                  </Stack>
                  <LinearProgress
                    aria-label={LEARN_MESSAGES.progressBarLabel}
                    sx={{
                      '& .MuiLinearProgress-bar': {
                        borderRadius: 5,
                      },
                      bgcolor: 'action.hover',
                      borderRadius: 5,
                      height: 10,
                    }}
                    value={progress.progressPercent}
                    variant="determinate"
                  />
                  <Typography color="text.secondary" sx={{ display: 'block', mt: 1 }} variant="body2">
                    {progress.completedStageCount} of {roadmap.stageCount} stages completed
                  </Typography>
                </Box>
              ) : null}

              {isRoadmapComplete ? (
                <Alert severity="success" sx={{ py: 0.5 }}>
                  {LEARN_MESSAGES.progressEnrollmentComplete}
                </Alert>
              ) : null}

              {hasEnrollment ? (
                <Box>
                  <Typography component="h3" sx={{ fontWeight: 700, mb: 0.5 }} variant="subtitle1">
                    {LEARN_MESSAGES.roadmapContinueLearning}
                  </Typography>
                  <Typography color="text.secondary" sx={{ mb: 2 }} variant="body2">
                    {LEARN_MESSAGES.roadmapWhatNext}
                  </Typography>

                  <Stepper
                    activeStep={activeStep}
                    alternativeLabel={!isMobile}
                    connector={<TimelineConnector />}
                    nonLinear
                    orientation={isMobile ? 'vertical' : 'horizontal'}
                    sx={{ mb: 2 }}
                  >
                    {roadmap.stages.map((stage, index) => {
                      const completed = progress.completedStageOrders.includes(stage.order)
                      const isCurrentStep = index === activeStep && !isRoadmapComplete
                      return (
                        <Step completed={completed} key={stage.slug}>
                          <StepLabel
                            onClick={() => {
                              document.getElementById(`stage-${stage.slug}`)?.scrollIntoView({ behavior: 'smooth' })
                            }}
                            slots={{ stepIcon: TimelineStepIcon }}
                            sx={{
                              '& .MuiStepLabel-label': {
                                color: completed ? 'success.main' : isCurrentStep ? 'primary.main' : 'text.secondary',
                                cursor: 'pointer',
                                fontWeight: isCurrentStep ? 700 : completed ? 600 : 400,
                                mt: isMobile ? 0 : 1,
                              },
                            }}
                          >
                            {stage.title}
                          </StepLabel>
                        </Step>
                      )
                    })}
                  </Stepper>

                  <Stack spacing={0.5}>
                    {!isRoadmapComplete && currentStage ? (
                      <Typography variant="body2">
                        {`${LEARN_MESSAGES.roadmapCurrentStage}: ${currentStage.title}`}
                      </Typography>
                    ) : null}
                    {progress.nextStageTitle && !isRoadmapComplete ? (
                      <Typography color="text.secondary" variant="body2">
                        {`${LEARN_MESSAGES.progressNextRecommendedStage}: ${progress.nextStageTitle}`}
                      </Typography>
                    ) : null}
                  </Stack>
                </Box>
              ) : (
                <Typography color="text.secondary" variant="body2">
                  {LEARN_MESSAGES.roadmapWhatIsThis}
                </Typography>
              )}

              {roadmap.sourceUrl ? (
                <Link href={roadmap.sourceUrl} rel="noopener noreferrer" target="_blank" variant="body2">
                  {roadmap.sourceUrl}
                </Link>
              ) : null}
            </Stack>
          </CardContent>
        </Card>

        <Stack spacing={2.5}>
          <Typography component="h2" sx={{ fontWeight: 700 }} variant="h6">
            Learning timeline
          </Typography>

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
