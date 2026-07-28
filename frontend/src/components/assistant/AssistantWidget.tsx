import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined'
import { Fab, Tooltip } from '@mui/material'
import { useEffect, useState } from 'react'
import { assistantApi } from '../../api/assistantApi'
import type { AssistantStatus } from '../../types/assistant'
import { resolveApiError } from '../../utils/apiErrors'
import { assistantMessages } from './assistantMessages'
import { AssistantChatPanel } from './AssistantChatPanel'

export function AssistantWidget() {
  const [open, setOpen] = useState(false)
  const [status, setStatus] = useState<AssistantStatus | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadStatus() {
      try {
        const nextStatus = await assistantApi.getStatus()
        if (!cancelled) {
          setStatus(nextStatus)
          setStatusError(null)
        }
      } catch (error) {
        if (!cancelled) {
          setStatusError(resolveApiError(error, assistantMessages.statusError))
        }
      }
    }

    void loadStatus()

    return () => {
      cancelled = true
    }
  }, [])

  const disabled = status !== null && !status.enabled
  const tooltipTitle = statusError
    ? statusError
    : disabled
      ? assistantMessages.disabledTitle
      : assistantMessages.open

  return (
    <>
      <Tooltip title={tooltipTitle}>
        <span>
          <Fab
            aria-label={assistantMessages.open}
            color="primary"
            disabled={disabled || status === null}
            onClick={() => setOpen((current) => !current)}
            sx={{
              position: 'fixed',
              right: 24,
              bottom: 24,
              zIndex: (theme) => theme.zIndex.drawer + 2,
            }}
          >
            <SmartToyOutlinedIcon />
          </Fab>
        </span>
      </Tooltip>
      <AssistantChatPanel onClose={() => setOpen(false)} open={open} status={status} />
    </>
  )
}
