import CloseIcon from '@mui/icons-material/Close'
import SendIcon from '@mui/icons-material/Send'
import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined'
import {
  Alert,
  Box,
  IconButton,
  Paper,
  TextField,
  Typography,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { assistantApi } from '../../api/assistantApi'
import type { AssistantStatus, AssistantUiMessage } from '../../types/assistant'
import { resolveApiError } from '../../utils/apiErrors'
import { assistantMessages } from './assistantMessages'
import { AssistantMessageList } from './AssistantMessageList'

interface AssistantChatPanelProps {
  open: boolean
  onClose: () => void
  status: AssistantStatus | null
}

function createMessageId(prefix: string) {
  return `${prefix}-${crypto.randomUUID()}`
}

export function AssistantChatPanel({ open, onClose, status }: AssistantChatPanelProps) {
  const [messages, setMessages] = useState<AssistantUiMessage[]>([])
  const [conversationId, setConversationId] = useState<string | null>(null)
  const [draft, setDraft] = useState('')
  const [historyLoading, setHistoryLoading] = useState(false)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const assistantEnabled = status?.enabled ?? false

  useEffect(() => {
    if (!open || !assistantEnabled) {
      return
    }

    let cancelled = false

    async function loadConversation() {
      setHistoryLoading(true)
      setError(null)
      try {
        const conversation = await assistantApi.getConversation()
        if (cancelled) {
          return
        }
        setConversationId(conversation.conversationId)
        setMessages(
          conversation.messages.map((message) => ({
            id: message.id,
            role: message.role,
            content: message.content,
            createdAt: message.createdAt,
          })),
        )
      } catch (loadError) {
        if (!cancelled) {
          setError(resolveApiError(loadError, assistantMessages.historyError))
        }
      } finally {
        if (!cancelled) {
          setHistoryLoading(false)
        }
      }
    }

    void loadConversation()

    return () => {
      cancelled = true
    }
  }, [open, assistantEnabled])

  async function handleSend(messageText: string) {
    const trimmed = messageText.trim()
    if (!trimmed || !assistantEnabled || sending) {
      return
    }

    const userMessage: AssistantUiMessage = {
      id: createMessageId('user'),
      role: 'USER',
      content: trimmed,
    }
    const pendingAssistantId = createMessageId('assistant-pending')

    setMessages((current) => [
      ...current,
      userMessage,
      { id: pendingAssistantId, role: 'ASSISTANT', content: '', pending: true },
    ])
    setDraft('')
    setSending(true)
    setError(null)

    try {
      const response = await assistantApi.sendMessage({
        message: trimmed,
        conversationId,
      })
      setConversationId(response.conversationId)
      setMessages((current) =>
        current
          .filter((message) => message.id !== pendingAssistantId)
          .concat({
            id: createMessageId('assistant'),
            role: 'ASSISTANT',
            content: response.response,
            intentType: response.intentType,
            metadata: response.metadata,
          }),
      )
    } catch (sendError) {
      setMessages((current) =>
        current
          .filter((message) => message.id !== pendingAssistantId)
          .concat({
            id: createMessageId('assistant-error'),
            role: 'ASSISTANT',
            content: resolveApiError(sendError, assistantMessages.sendError),
            error: true,
          }),
      )
      setError(resolveApiError(sendError, assistantMessages.sendError))
    } finally {
      setSending(false)
    }
  }

  if (!open) {
    return null
  }

  return (
    <Paper
      aria-label={assistantMessages.title}
      elevation={8}
      role="dialog"
      sx={{
        position: 'fixed',
        right: 24,
        bottom: 96,
        width: { xs: 'calc(100vw - 32px)', sm: 380 },
        maxWidth: 380,
        height: 520,
        display: 'flex',
        flexDirection: 'column',
        zIndex: (theme) => theme.zIndex.drawer + 2,
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 2,
          py: 1.5,
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <SmartToyOutlinedIcon color="primary" fontSize="small" />
          <Typography component="h2" variant="subtitle1">
            {assistantMessages.title}
          </Typography>
        </Box>
        <IconButton aria-label={assistantMessages.close} onClick={onClose} size="small">
          <CloseIcon fontSize="small" />
        </IconButton>
      </Box>

      {!assistantEnabled ? (
        <Box sx={{ p: 2 }}>
          <Alert severity="info">{assistantMessages.disabledDescription}</Alert>
        </Box>
      ) : (
        <>
          {error ? (
            <Box sx={{ px: 2, pt: 2 }}>
              <Alert severity="error">{error}</Alert>
            </Box>
          ) : null}
          <AssistantMessageList
            loading={historyLoading || sending}
            messages={messages}
            onExampleClick={(example) => {
              void handleSend(example)
            }}
          />
          <Box
            component="form"
            onSubmit={(event) => {
              event.preventDefault()
              void handleSend(draft)
            }}
            sx={{
              display: 'flex',
              gap: 1,
              p: 2,
              borderTop: 1,
              borderColor: 'divider',
            }}
          >
            <TextField
              disabled={historyLoading || sending}
              fullWidth
              onChange={(event) => setDraft(event.target.value)}
              placeholder={assistantMessages.placeholder}
              size="small"
              slotProps={{ htmlInput: { 'aria-label': assistantMessages.placeholder } }}
              value={draft}
            />
            <IconButton
              aria-label={assistantMessages.send}
              color="primary"
              disabled={!draft.trim() || historyLoading || sending}
              type="submit"
            >
              <SendIcon />
            </IconButton>
          </Box>
        </>
      )}
    </Paper>
  )
}
