import { useEffect, useMemo, useState } from 'react'
import MapOutlinedIcon from '@mui/icons-material/MapOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Link,
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
import type { Roadmap } from '../../types/roadmap'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function RoadmapPage() {
  const { technologyId } = useParams()
  const [roadmap, setRoadmap] = useState<Roadmap | null>(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [activeStep, setActiveStep] = useState(0)

  useEffect(() => {
    if (!technologyId) {
      setNotFound(true)
      setLoading(false)
      return
    }

    let mounted = true

    async function loadRoadmap() {
      setLoading(true)
      setError(null)
      setNotFound(false)

      try {
        const response = await learnApi.getRoadmapByTechnologyId(technologyId!)
        if (mounted) {
          setRoadmap(response)
          const recommendedIndex = response.stages.findIndex(
            (stage) => stage.order === response.recommendedStageOrder,
          )
          setActiveStep(recommendedIndex >= 0 ? recommendedIndex : 0)
        }
      } catch (loadError) {
        if (mounted) {
          if (isNotFoundError(loadError)) {
            setNotFound(true)
          } else {
            setError(resolveApiError(loadError, LEARN_MESSAGES.roadmapLoadError))
          }
          setRoadmap(null)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadRoadmap()

    return () => {
      mounted = false
    }
  }, [technologyId])

  const remainingStages = useMemo(() => {
    if (!roadmap) {
      return 0
    }
    return Math.max(roadmap.stageCount - activeStep - 1, 0)
  }, [roadmap, activeStep])

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
                <Chip label={`${LEARN_MESSAGES.roadmapVersion}: ${roadmap.version}`} />
                {roadmap.source ? (
                  <Chip label={`${LEARN_MESSAGES.roadmapCatalogSource}: ${roadmap.source}`} variant="outlined" />
                ) : null}
              </Stack>
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

        <Card variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">{LEARN_MESSAGES.roadmapContinueLearning}</Typography>
              <Typography color="text.secondary" variant="body2">
                {LEARN_MESSAGES.roadmapWhatNext}
              </Typography>
              <Stepper activeStep={activeStep} alternativeLabel nonLinear>
                {roadmap.stages.map((stage, index) => (
                  <Step completed={index < activeStep} key={stage.slug}>
                    <StepLabel
                      onClick={() => setActiveStep(index)}
                      sx={{ cursor: 'pointer' }}
                    >
                      {stage.title}
                    </StepLabel>
                  </Step>
                ))}
              </Stepper>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                <Chip label={`${LEARN_MESSAGES.roadmapCurrentStage}: ${currentStage.title}`} color="success" />
                {roadmap.nextStageOrder > roadmap.recommendedStageOrder ? (
                  <Chip
                    label={`${LEARN_MESSAGES.roadmapNextStage}: ${
                      roadmap.stages.find((stage) => stage.order === roadmap.nextStageOrder)?.title ?? '—'
                    }`}
                    color="info"
                    variant="outlined"
                  />
                ) : null}
                <Chip label={`${LEARN_MESSAGES.roadmapRemaining}: ${remainingStages}`} variant="outlined" />
              </Stack>
            </Stack>
          </CardContent>
        </Card>

        {roadmap.stages.map((stage, index) => (
          <RoadmapStageCard
            isNext={stage.order === roadmap.nextStageOrder}
            isRecommended={stage.order === roadmap.recommendedStageOrder}
            key={stage.slug}
            stage={stage}
            stageNumber={index + 1}
            totalStages={roadmap.stageCount}
          />
        ))}
      </Stack>
    </>
  )
}
