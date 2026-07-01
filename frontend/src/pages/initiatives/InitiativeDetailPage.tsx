import { useCallback, useEffect, useState } from 'react'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import { Alert, Box, Button, Stack } from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'
import { initiativesApi } from '../../api/initiativesApi'
import type { InitiativeLeaderboardEntry } from '../../api/leaderboardsApi'
import { leaderboardsApi } from '../../api/leaderboardsApi'
import { submissionsApi } from '../../api/submissionsApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { DetailPageSkeleton } from '../../components/initiatives/DetailPageSkeleton'
import { InitiativeDetailBackLink } from '../../components/initiatives/InitiativeDetailBackLink'
import { InitiativeActionBar } from '../../components/initiatives/InitiativeActionBar'
import { InitiativeDescriptionCard } from '../../components/initiatives/InitiativeDescriptionCard'
import { EditInitiativeDialog } from '../../components/initiatives/EditInitiativeDialog'
import { InitiativeDetailAlerts } from '../../components/initiatives/InitiativeDetailAlerts'
import { InitiativeNotFoundPanel } from '../../components/initiatives/InitiativeNotFoundPanel'
import { InitiativeRewardCard } from '../../components/initiatives/InitiativeRewardCard'
import { InitiativeDeleteAction } from '../../components/initiatives/InitiativeDeleteAction'
import { InitiativeLifecycleActions } from '../../components/initiatives/InitiativeLifecycleActions'
import { InitiativeStatusChip } from '../../components/initiatives/InitiativeStatusChip'
import { INITIATIVE_MESSAGES } from '../../components/initiatives/initiativeMessages'
import { MyProgressCard } from '../../components/initiatives/MyProgressCard'
import { TopLearnerCard } from '../../components/initiatives/TopLearnerCard'
import {
  InitiativeManagementSnackbar,
  type InitiativeManagementNotification,
} from '../../components/initiatives/InitiativeManagementSnackbar'
import type { Initiative, InitiativeLifecycleAction } from '../../types/initiatives'
import { formatInitiativeDateRange } from '../../components/initiatives/initiativeDisplay'
import type { CertificateSubmission } from '../../types/submissions'
import { isNotFoundError, resolveApiError } from '../../utils/apiErrors'

function lifecycleSuccessMessage(action: InitiativeLifecycleAction) {
  switch (action) {
    case 'publish':
      return INITIATIVE_MESSAGES.publishSuccess
    case 'returnToDraft':
      return INITIATIVE_MESSAGES.returnToDraftSuccess
    case 'markExpired':
      return INITIATIVE_MESSAGES.markExpiredSuccess
    case 'reactivate':
      return INITIATIVE_MESSAGES.reactivateSuccess
  }
}

interface SecondaryDetailData {
  submission: CertificateSubmission | null
  submissionError: string | null
  topLearner: InitiativeLeaderboardEntry | null
  topLearnerError: string | null
}

const EMPTY_SECONDARY: SecondaryDetailData = {
  submission: null,
  submissionError: null,
  topLearner: null,
  topLearnerError: null,
}

export function InitiativeDetailPage() {
  const navigate = useNavigate()
  const { initiativeId } = useParams()
  const { isAdmin, isEmployee } = useAuth()
  const [initiative, setInitiative] = useState<Initiative | null>(null)
  const [secondary, setSecondary] = useState<SecondaryDetailData>(EMPTY_SECONDARY)
  const [loadingPrimary, setLoadingPrimary] = useState(true)
  const [loadingSecondary, setLoadingSecondary] = useState(false)
  const [notFound, setNotFound] = useState(false)
  const [primaryError, setPrimaryError] = useState<string | null>(null)
  const [refreshToken, setRefreshToken] = useState(0)
  const [editOpen, setEditOpen] = useState(false)
  const [notification, setNotification] = useState<InitiativeManagementNotification | null>(null)

  const loadSecondaryData = useCallback(async (id: string, employeeView: boolean) => {
    setLoadingSecondary(true)
    setSecondary(EMPTY_SECONDARY)

    const requests: Array<Promise<unknown>> = [
      leaderboardsApi
        .initiative(id, { size: 1, sort: 'rank,asc' })
        .then((response) => ({ type: 'topLearner' as const, value: response.content[0] ?? null }))
        .catch((error) => ({ type: 'topLearnerError' as const, value: resolveApiError(error) })),
    ]

    if (employeeView) {
      requests.push(
        submissionsApi
          .listMine({ initiativeId: id, size: 1 })
          .then((response) => ({ type: 'submission' as const, value: response.content[0] ?? null }))
          .catch((error) => ({ type: 'submissionError' as const, value: resolveApiError(error) })),
      )
    }

    const results = await Promise.allSettled(requests)
    const nextSecondary: SecondaryDetailData = { ...EMPTY_SECONDARY }

    for (const result of results) {
      if (result.status !== 'fulfilled') {
        continue
      }

      const payload = result.value as
        | { type: 'topLearner'; value: InitiativeLeaderboardEntry | null }
        | { type: 'topLearnerError'; value: string }
        | { type: 'submission'; value: CertificateSubmission | null }
        | { type: 'submissionError'; value: string }

      switch (payload.type) {
        case 'topLearner':
          nextSecondary.topLearner = payload.value
          break
        case 'topLearnerError':
          nextSecondary.topLearnerError = payload.value
          break
        case 'submission':
          nextSecondary.submission = payload.value
          break
        case 'submissionError':
          nextSecondary.submissionError = INITIATIVE_MESSAGES.progressLoadError
          break
      }
    }

    setSecondary(nextSecondary)
    setLoadingSecondary(false)
  }, [])

  useEffect(() => {
    let mounted = true

    async function loadInitiative() {
      if (!initiativeId) {
        if (mounted) {
          setNotFound(true)
          setInitiative(null)
          setPrimaryError(null)
          setLoadingPrimary(false)
          setSecondary(EMPTY_SECONDARY)
        }
        return
      }

      setLoadingPrimary(true)
      setPrimaryError(null)
      setNotFound(false)
      setInitiative(null)
      setSecondary(EMPTY_SECONDARY)

      try {
        const response = await initiativesApi.get(initiativeId)
        if (!mounted) {
          return
        }

        setInitiative(response)
        setLoadingPrimary(false)
        await loadSecondaryData(initiativeId, isEmployee)
      } catch (error) {
        if (!mounted) {
          return
        }

        if (isNotFoundError(error)) {
          setNotFound(true)
          setPrimaryError(null)
        } else {
          setPrimaryError(resolveApiError(error, INITIATIVE_MESSAGES.detailLoadError))
        }
        setInitiative(null)
        setLoadingPrimary(false)
      }
    }

    void loadInitiative()

    return () => {
      mounted = false
    }
  }, [initiativeId, isEmployee, loadSecondaryData, refreshToken])

  if (!initiativeId || notFound) {
    return <InitiativeNotFoundPanel />
  }

  if (loadingPrimary) {
    return <DetailPageSkeleton />
  }

  if (primaryError || !initiative) {
    return (
      <Alert
        action={
          <Button color="inherit" onClick={() => setRefreshToken((current) => current + 1)} size="small">
            {INITIATIVE_MESSAGES.retry}
          </Button>
        }
        severity="error"
      >
        {primaryError ?? INITIATIVE_MESSAGES.detailLoadError}
      </Alert>
    )
  }

  const rewardDescription = initiative.rewardDescription?.trim()

  const progressCard = isEmployee ? (
    <MyProgressCard
      error={secondary.submissionError}
      loading={loadingSecondary}
      submission={secondary.submission}
    />
  ) : null

  const actionBar = isEmployee ? (
    <InitiativeActionBar
      initiativeId={initiative.id}
      loading={loadingSecondary}
      submission={secondary.submission}
    />
  ) : null

  const descriptionCard = <InitiativeDescriptionCard description={initiative.description} />

  const rewardCard = rewardDescription ? <InitiativeRewardCard rewardDescription={rewardDescription} /> : null

  const topLearnerCard = (
    <TopLearnerCard
      entry={secondary.topLearner}
      error={secondary.topLearnerError}
      loading={loadingSecondary}
    />
  )

  return (
    <>
      <InitiativeDetailBackLink />
      <PageHeader
        description={formatInitiativeDateRange(initiative.startDateUtc, initiative.expiryDateUtc)}
        title={initiative.title}
      />

      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', justifyContent: 'space-between', mb: 2 }}>
        <InitiativeStatusChip status={initiative.status} />
        {isAdmin ? (
          <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
            <InitiativeLifecycleActions
              initiative={initiative}
              layout="buttons"
              onSuccess={(action) => {
                setNotification({ message: lifecycleSuccessMessage(action), severity: 'success' })
                setRefreshToken((current) => current + 1)
              }}
            />
            <Button onClick={() => setEditOpen(true)} startIcon={<EditOutlinedIcon />} variant="outlined">
              Edit
            </Button>
            <InitiativeDeleteAction
              initiative={initiative}
              layout="button"
              onSuccess={() => {
                setNotification({ message: INITIATIVE_MESSAGES.deleteSuccess, severity: 'success' })
                navigate('/initiatives')
              }}
            />
          </Stack>
        ) : null}
      </Stack>

      <EditInitiativeDialog
        initiative={initiative}
        onClose={() => setEditOpen(false)}
        onSuccess={() => {
          setEditOpen(false)
          setNotification({ message: INITIATIVE_MESSAGES.updateSuccess, severity: 'success' })
          setRefreshToken((current) => current + 1)
        }}
        open={editOpen}
      />

      <InitiativeManagementSnackbar notification={notification} onClose={() => setNotification(null)} />

      <InitiativeDetailAlerts initiative={initiative} />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { md: '2fr 1fr', xs: '1fr' },
          minWidth: 0,
        }}
      >
        {isEmployee ? (
          <Box sx={{ gridColumn: { md: 2 }, gridRow: { md: 1 }, order: { xs: 1 } }}>{progressCard}</Box>
        ) : null}
        {isEmployee ? (
          <Box sx={{ gridColumn: { md: 1 }, gridRow: { md: 3 }, order: { xs: 2 } }}>{actionBar}</Box>
        ) : null}
        <Box sx={{ gridColumn: { md: 1 }, gridRow: { md: 1 }, order: { xs: 3 } }}>{descriptionCard}</Box>
        {rewardCard ? (
          <Box sx={{ gridColumn: { md: 1 }, gridRow: { md: 2 }, order: { xs: 4 } }}>{rewardCard}</Box>
        ) : null}
        <Box
          sx={{
            gridColumn: { md: 2 },
            gridRow: { md: isEmployee ? 2 : 1 },
            order: { xs: 5 },
          }}
        >
          {topLearnerCard}
        </Box>
      </Box>
    </>
  )
}
