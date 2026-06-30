import { useEffect, useState } from 'react'
import EventBusyOutlinedIcon from '@mui/icons-material/EventBusyOutlined'
import PublishOutlinedIcon from '@mui/icons-material/PublishOutlined'
import ReplayOutlinedIcon from '@mui/icons-material/ReplayOutlined'
import UnpublishedOutlinedIcon from '@mui/icons-material/UnpublishedOutlined'
import { Alert, Button, IconButton, Stack, TextField, Tooltip, Typography } from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import type { Initiative, InitiativeLifecycleAction } from '../../types/initiatives'
import { resolveApiError } from '../../utils/apiErrors'
import { formatInitiativeDate } from './initiativeDisplay'
import {
  InitiativeLifecycleConfirmDialog,
  InitiativeLifecycleSummary,
} from './InitiativeLifecycleConfirmDialog'
import { isUtcDateBefore, isUtcDateOnOrAfter, todayUtcDateInput, utcDateInputToInstant } from './initiativeDateUtils'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeLifecycleActionsProps {
  initiative: Initiative
  layout?: 'compact' | 'buttons'
  disabled?: boolean
  onSuccess: (action: InitiativeLifecycleAction, updated: Initiative) => void
}

interface PendingAction {
  action: InitiativeLifecycleAction
  initiative: Initiative
}

function instantToFormDate(instant: string) {
  const parsed = Date.parse(instant)
  if (!Number.isFinite(parsed)) {
    return todayUtcDateInput()
  }

  const date = new Date(parsed)
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  const day = String(date.getUTCDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function InitiativeLifecycleActions({
  initiative,
  layout = 'compact',
  disabled = false,
  onSuccess,
}: InitiativeLifecycleActionsProps) {
  const [pending, setPending] = useState<PendingAction | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [reactivateExpiryDate, setReactivateExpiryDate] = useState('')
  const [reactivateExpiryError, setReactivateExpiryError] = useState<string | null>(null)

  useEffect(() => {
    if (!pending || pending.action !== 'reactivate') {
      return
    }

    setReactivateExpiryDate(instantToFormDate(pending.initiative.expiryDateUtc))
    setReactivateExpiryError(null)
    setError(null)
  }, [pending])

  const availableActions = getAvailableLifecycleActions(initiative.status)

  function openAction(action: InitiativeLifecycleAction) {
    setPending({ action, initiative })
    setError(null)
    setReactivateExpiryError(null)
  }

  function closeDialog() {
    if (submitting) {
      return
    }
    setPending(null)
    setError(null)
    setReactivateExpiryError(null)
  }

  function validateReactivateExpiryDate(expiryDate: string, startDateUtc: string) {
    const today = todayUtcDateInput()
    if (!expiryDate) {
      return INITIATIVE_MESSAGES.reactivateExpiryDateRequired
    }
    if (isUtcDateBefore(expiryDate, today)) {
      return INITIATIVE_MESSAGES.reactivateExpiryDateBeforeToday
    }
    const startDate = instantToFormDate(startDateUtc)
    if (!isUtcDateOnOrAfter(expiryDate, startDate)) {
      return INITIATIVE_MESSAGES.formDateRangeInvalid
    }
    return null
  }

  async function handleConfirm() {
    if (!pending) {
      return
    }

    if (pending.action === 'reactivate') {
      const validationError = validateReactivateExpiryDate(reactivateExpiryDate, pending.initiative.startDateUtc)
      if (validationError) {
        setReactivateExpiryError(validationError)
        return
      }
    }

    setSubmitting(true)
    setError(null)

    try {
      const updated = await executeLifecycleAction(pending.action, pending.initiative.id, reactivateExpiryDate)
      onSuccess(pending.action, updated)
      setPending(null)
    } catch (actionError) {
      setError(resolveApiError(actionError, getLifecycleErrorMessage(pending.action)))
    } finally {
      setSubmitting(false)
    }
  }

  if (availableActions.length === 0) {
    return null
  }

  const actionButtons = availableActions.map((action) => {
    const config = LIFECYCLE_ACTION_CONFIG[action]
    const button = layout === 'buttons' ? (
      <Button
        color={config.color}
        disabled={disabled || submitting}
        key={action}
        onClick={(event) => {
          event.stopPropagation()
          openAction(action)
        }}
        size="small"
        startIcon={config.icon}
        variant="outlined"
      >
        {config.label}
      </Button>
    ) : (
      <Tooltip key={action} title={config.label}>
        <span>
          <IconButton
            aria-label={`${config.label} ${initiative.title}`}
            color={config.color}
            disabled={disabled || submitting}
            onClick={(event) => {
              event.stopPropagation()
              openAction(action)
            }}
            size="small"
          >
            {config.icon}
          </IconButton>
        </span>
      </Tooltip>
    )

    return button
  })

  const confirmConfig = pending ? LIFECYCLE_CONFIRM_CONFIG[pending.action] : null
  const reactivateStartDate = pending ? instantToFormDate(pending.initiative.startDateUtc) : ''

  return (
    <>
      <Stack
        direction="row"
        spacing={0.5}
        sx={{ alignItems: 'center', flexWrap: 'wrap' }}
      >
        {actionButtons}
      </Stack>

      {pending && confirmConfig ? (
        <InitiativeLifecycleConfirmDialog
          confirmColor={confirmConfig.confirmColor}
          confirmDisabled={pending.action === 'reactivate' && Boolean(reactivateExpiryError)}
          confirmLabel={confirmConfig.confirmLabel}
          onClose={closeDialog}
          onConfirm={() => void handleConfirm()}
          open
          submitting={submitting}
          title={confirmConfig.title}
        >
          {error ? <Alert severity="error">{error}</Alert> : null}
          {pending.action === 'publish' ? (
            <>
              <InitiativeLifecycleSummary
                expiryDate={formatInitiativeDate(pending.initiative.expiryDateUtc)}
                startDate={formatInitiativeDate(pending.initiative.startDateUtc)}
                title={pending.initiative.title}
              />
              <Typography>{INITIATIVE_MESSAGES.confirmPublishIntro}</Typography>
            </>
          ) : null}
          {pending.action === 'returnToDraft' ? (
            <Typography>{INITIATIVE_MESSAGES.confirmReturnToDraftBody}</Typography>
          ) : null}
          {pending.action === 'markExpired' ? (
            <Typography>{INITIATIVE_MESSAGES.confirmMarkExpiredBody}</Typography>
          ) : null}
          {pending.action === 'reactivate' ? (
            <>
              <Typography>{INITIATIVE_MESSAGES.confirmReactivateBody}</Typography>
              <TextField
                error={Boolean(reactivateExpiryError)}
                fullWidth
                helperText={reactivateExpiryError ?? INITIATIVE_MESSAGES.formUtcDateHelper}
                label={INITIATIVE_MESSAGES.reactivateExpiryDate}
                onChange={(event) => {
                  const nextValue = event.target.value
                  setReactivateExpiryDate(nextValue)
                  setReactivateExpiryError(
                    validateReactivateExpiryDate(nextValue, pending.initiative.startDateUtc),
                  )
                }}
                required
                slotProps={{
                  htmlInput: { min: reactivateStartDate },
                  inputLabel: { shrink: true },
                }}
                type="date"
                value={reactivateExpiryDate}
              />
            </>
          ) : null}
        </InitiativeLifecycleConfirmDialog>
      ) : null}
    </>
  )
}

export function getAvailableLifecycleActions(status: Initiative['status']): InitiativeLifecycleAction[] {
  switch (status) {
    case 'DRAFT':
      return ['publish']
    case 'ACTIVE':
      return ['returnToDraft', 'markExpired']
    case 'EXPIRED':
      return ['reactivate']
    default:
      return []
  }
}

const LIFECYCLE_ACTION_CONFIG = {
  publish: {
    label: INITIATIVE_MESSAGES.lifecyclePublish,
    color: 'success' as const,
    icon: <PublishOutlinedIcon fontSize="small" />,
  },
  returnToDraft: {
    label: INITIATIVE_MESSAGES.lifecycleReturnToDraft,
    color: 'warning' as const,
    icon: <UnpublishedOutlinedIcon fontSize="small" />,
  },
  markExpired: {
    label: INITIATIVE_MESSAGES.lifecycleMarkExpired,
    color: 'error' as const,
    icon: <EventBusyOutlinedIcon fontSize="small" />,
  },
  reactivate: {
    label: INITIATIVE_MESSAGES.lifecycleReactivate,
    color: 'success' as const,
    icon: <ReplayOutlinedIcon fontSize="small" />,
  },
} as const

const LIFECYCLE_CONFIRM_CONFIG = {
  publish: {
    title: INITIATIVE_MESSAGES.confirmPublishTitle,
    confirmLabel: INITIATIVE_MESSAGES.lifecyclePublish,
    confirmColor: 'success' as const,
  },
  returnToDraft: {
    title: INITIATIVE_MESSAGES.confirmReturnToDraftTitle,
    confirmLabel: INITIATIVE_MESSAGES.lifecycleReturnToDraft,
    confirmColor: 'warning' as const,
  },
  markExpired: {
    title: INITIATIVE_MESSAGES.confirmMarkExpiredTitle,
    confirmLabel: INITIATIVE_MESSAGES.lifecycleMarkExpired,
    confirmColor: 'error' as const,
  },
  reactivate: {
    title: INITIATIVE_MESSAGES.confirmReactivateTitle,
    confirmLabel: INITIATIVE_MESSAGES.lifecycleReactivate,
    confirmColor: 'success' as const,
  },
} as const

function getLifecycleErrorMessage(action: InitiativeLifecycleAction) {
  switch (action) {
    case 'publish':
      return INITIATIVE_MESSAGES.publishError
    case 'returnToDraft':
      return INITIATIVE_MESSAGES.returnToDraftError
    case 'markExpired':
      return INITIATIVE_MESSAGES.markExpiredError
    case 'reactivate':
      return INITIATIVE_MESSAGES.reactivateError
  }
}

async function executeLifecycleAction(
  action: InitiativeLifecycleAction,
  initiativeId: string,
  reactivateExpiryDate: string,
) {
  switch (action) {
    case 'publish':
      return initiativesApi.publish(initiativeId)
    case 'returnToDraft':
      return initiativesApi.returnToDraft(initiativeId)
    case 'markExpired':
      return initiativesApi.markExpired(initiativeId)
    case 'reactivate':
      return initiativesApi.reactivate(initiativeId, {
        expiryDateUtc: utcDateInputToInstant(reactivateExpiryDate),
      })
  }
}
