import { useEffect, useState } from 'react'
import TuneOutlinedIcon from '@mui/icons-material/TuneOutlined'
import { Alert, Box, Button, Card, CardContent, Link, Stack, Typography } from '@mui/material'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TechnologyCurationPanel } from '../../components/learn/TechnologyCurationPanel'
import { RelatedOrganizationProjectsCard } from '../../components/learn/RelatedOrganizationProjectsCard'
import { TechnologyCategoryChip, TechnologyDifficultyChip } from '../../components/learn/TechnologyMetaChips'
import { TechnologyLifecycleActions } from '../../components/learn/TechnologyLifecycleActions'
import { TechnologyStatusChip } from '../../components/learn/TechnologyStatusChip'
import {
  LearnManagementSnackbar,
  type LearnManagementNotification,
} from '../../components/learn/LearnManagementSnackbar'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import type { Technology, TechnologyLifecycleAction } from '../../types/learn'
import type { Enrollment } from '../../types/progress'
import { isConflictError, isNotFoundError, resolveApiError } from '../../utils/apiErrors'

export function TechnologyDetailPage() {
  const { technologyId } = useParams()
  const navigate = useNavigate()
  const { isAdmin } = useAuth()
  const [technology, setTechnology] = useState<Technology | null>(null)
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null)
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [curationOpen, setCurationOpen] = useState(false)
  const [startingLearning, setStartingLearning] = useState(false)
  const [notification, setNotification] = useState<LearnManagementNotification | null>(null)

  useEffect(() => {
    if (!technologyId) {
      setNotFound(true)
      setLoading(false)
      return
    }

    let mounted = true

    async function loadTechnology() {
      setLoading(true)
      setError(null)
      setNotFound(false)

      try {
        const response = await learnApi.getTechnology(technologyId!)
        if (mounted) {
          setTechnology(response)
        }

        try {
          const enrollmentResponse = await learnApi.getActiveEnrollment(technologyId!)
          if (mounted) {
            setEnrollment(enrollmentResponse)
          }
        } catch (enrollmentError) {
          if (mounted && !isNotFoundError(enrollmentError)) {
            throw enrollmentError
          }
          if (mounted) {
            setEnrollment(null)
          }
        }
      } catch (loadError) {
        if (mounted) {
          if (isNotFoundError(loadError)) {
            setNotFound(true)
          } else {
            setError(resolveApiError(loadError, LEARN_MESSAGES.detailLoadError))
          }
          setTechnology(null)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    void loadTechnology()

    return () => {
      mounted = false
    }
  }, [technologyId])

  function reloadTechnology() {
    if (!technologyId) {
      return
    }
    void learnApi.getTechnology(technologyId).then(setTechnology).catch(() => undefined)
    void learnApi.getActiveEnrollment(technologyId)
      .then(setEnrollment)
      .catch((enrollmentError) => {
        if (isNotFoundError(enrollmentError)) {
          setEnrollment(null)
        }
      })
  }

  function handleLifecycleSuccess(action: TechnologyLifecycleAction) {
    const message =
      action === 'publish'
        ? LEARN_MESSAGES.publishSuccess
        : action === 'hide'
          ? LEARN_MESSAGES.hideSuccess
          : LEARN_MESSAGES.archiveSuccess
    setNotification({ message, severity: 'success' })
    if (!technologyId) {
      return
    }
    void learnApi.getTechnology(technologyId).then(setTechnology).catch(() => undefined)
  }

  async function handleStartLearning() {
    if (!technologyId) {
      return
    }

    setStartingLearning(true)
    try {
      const createdEnrollment = await learnApi.enrollInTechnology(technologyId)
      setEnrollment(createdEnrollment)
      setNotification({ message: LEARN_MESSAGES.progressEnrollSuccess, severity: 'success' })
      navigate(`/learn/technologies/${technologyId}/roadmap`)
    } catch (startError) {
      if (isConflictError(startError)) {
        navigate(`/learn/technologies/${technologyId}/roadmap`)
        return
      }
      setNotification({
        message: resolveApiError(startError, LEARN_MESSAGES.progressEnrollError),
        severity: 'error',
      })
    } finally {
      setStartingLearning(false)
    }
  }

  const hasActiveEnrollment =
    enrollment != null && (enrollment.status === 'IN_PROGRESS' || enrollment.status === 'NOT_STARTED')
  const isRoadmapComplete = enrollment?.status === 'COMPLETED'

  if (loading) {
    return <Alert severity="info">Loading technology...</Alert>
  }

  if (notFound) {
    return (
      <Alert severity="warning">
        {LEARN_MESSAGES.detailNotFound}
        <Box sx={{ mt: 2 }}>
          <Button component={RouterLink} to="/learn/technologies" variant="outlined">
            {LEARN_MESSAGES.backToTechnologies}
          </Button>
        </Box>
      </Alert>
    )
  }

  if (error || !technology) {
    return <Alert severity="error">{error ?? LEARN_MESSAGES.detailLoadError}</Alert>
  }

  return (
    <>
      <PageHeader
        description={LEARN_MESSAGES.technologyDetailDescription}
        title={technology.name}
      />

      <Stack spacing={1} sx={{ mb: 3 }}>
        <Button component={RouterLink} to="/learn/technologies" variant="text">
          {LEARN_MESSAGES.backToTechnologies}
        </Button>
        <Stack direction="row" spacing={1}>
          <TechnologyCategoryChip category={technology.category} />
          <TechnologyDifficultyChip difficulty={technology.difficulty} />
          {isAdmin ? <TechnologyStatusChip status={technology.status} /> : null}
        </Stack>
      </Stack>

      <Stack spacing={3}>
        <Card variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">About this technology</Typography>
              <Typography>{technology.description || technology.shortName}</Typography>
              {technology.estimatedDuration ? (
                <Typography color="text.secondary" variant="body2">
                  {LEARN_MESSAGES.curationEstimatedDuration}: {technology.estimatedDuration}
                </Typography>
              ) : null}
              <Stack direction="row" spacing={2}>
                {technology.officialWebsite ? (
                  <Link href={technology.officialWebsite} rel="noopener noreferrer" target="_blank">
                    {LEARN_MESSAGES.curationOfficialWebsite}
                  </Link>
                ) : null}
                {technology.officialDocumentation ? (
                  <Link href={technology.officialDocumentation} rel="noopener noreferrer" target="_blank">
                    {LEARN_MESSAGES.curationOfficialDocumentation}
                  </Link>
                ) : null}
              </Stack>
              <Typography color="text.secondary" variant="body2">
                {LEARN_MESSAGES.whatNext}
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1}>
                {!hasActiveEnrollment && !isRoadmapComplete ? (
                  <Button disabled={startingLearning} onClick={() => void handleStartLearning()} variant="contained">
                    {LEARN_MESSAGES.progressStartLearning}
                  </Button>
                ) : null}
                {hasActiveEnrollment ? (
                  <Button
                    component={RouterLink}
                    to={`/learn/technologies/${technology.id}/roadmap`}
                    variant="contained"
                  >
                    {LEARN_MESSAGES.progressContinueLearning}
                  </Button>
                ) : null}
                {isRoadmapComplete ? (
                  <Button
                    component={RouterLink}
                    to={`/learn/technologies/${technology.id}/roadmap`}
                    variant="contained"
                  >
                    {LEARN_MESSAGES.progressRoadmapComplete}
                  </Button>
                ) : null}
                <Button
                  component={RouterLink}
                  to={`/learn/technologies/${technology.id}/roadmap`}
                  variant={hasActiveEnrollment || isRoadmapComplete ? 'outlined' : 'text'}
                >
                  {LEARN_MESSAGES.viewRoadmap}
                </Button>
              </Stack>
              <Typography color="text.secondary" variant="caption">
                {LEARN_MESSAGES.viewRoadmapHelper}
              </Typography>
            </Stack>
          </CardContent>
        </Card>

        <RelatedOrganizationProjectsCard projects={technology.relatedProjects ?? []} />

        {isAdmin ? (
          <Card variant="outlined">
            <CardContent>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ justifyContent: 'space-between' }}>
                <Stack spacing={1}>
                  <Typography variant="h6">{LEARN_MESSAGES.curationAdminTitle}</Typography>
                  <Typography color="text.secondary" variant="body2">
                    {LEARN_MESSAGES.curationAdminDescription}
                  </Typography>
                </Stack>
                <Stack direction="row" spacing={1}>
                  <TechnologyLifecycleActions
                    onError={(message) => setNotification({ message, severity: 'error' })}
                    onSuccess={handleLifecycleSuccess}
                    technology={technology}
                  />
                  <Button onClick={() => setCurationOpen(true)} startIcon={<TuneOutlinedIcon />} variant="outlined">
                    {LEARN_MESSAGES.curationAction}
                  </Button>
                </Stack>
              </Stack>
            </CardContent>
          </Card>
        ) : null}
      </Stack>

      {isAdmin ? (
        <TechnologyCurationPanel
          onClose={() => setCurationOpen(false)}
          onSuccess={() => {
            setCurationOpen(false)
            setNotification({ message: LEARN_MESSAGES.curationSuccess, severity: 'success' })
            reloadTechnology()
          }}
          open={curationOpen}
          technology={technology}
        />
      ) : null}

      <LearnManagementSnackbar notification={notification} onClose={() => setNotification(null)} />
    </>
  )
}
