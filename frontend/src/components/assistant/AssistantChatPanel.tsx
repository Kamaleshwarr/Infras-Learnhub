import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined'
import CloseIcon from '@mui/icons-material/Close'
import SendIcon from '@mui/icons-material/Send'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  IconButton,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useCallback, useEffect, useRef, useState, type KeyboardEvent } from 'react'
import { assistantApi } from '../../api/assistantApi'
import type { ConversationMessage } from '../../types/assistant'
import { resolveApiError } from '../../utils/apiErrors'
import { AssistantMessageBubble } from './AssistantMessageBubble'
import { assistantMessages } from './assistantMessages'

interface AssistantChatPanelProps {
  open: boolean
  onClose: () => void
}

function isDisplayableMessage(message: ConversationMessage) {
  return message.role === 'USER' || message.role === 'ASSISTANT'
}

export function AssistantChatPanel({ open, onClose }: AssistantChatPanelProps) {
  const [messages, setMessages] = useState<ConversationMessage[]>([])
  const [conversationId, setConversationId] = useState<string | null>(null)
  const [inputValue, setInputValue] = useState('')
  const [loadingConversation, setLoadingConversation] = useState(false)
  const [sending, setSending] = useState(false)
  const [conversationError, setConversationError] = useState<string | null>(null)
  const [chatError, setChatError] = useState<string | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const mountedRef = useRef(true)

  const scrollToBottom = useCallback(() => {
    const element = messagesEndRef.current
    if (element && typeof element.scrollIntoView === 'function') {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  }, [])

  const loadConversation = useCallback(async () => {
    setLoadingConversation(true)
    setConversationError(null)
    try {
      const conversation = await assistantApi.getConversation()
      if (!mountedRef.current) {
        return
      }
      setConversationId(conversation.conversationId)
      setMessages(conversation.messages.filter(isDisplayableMessage))
    } catch (error) {
      if (!mountedRef.current) {
        return
      }
      setConversationError(resolveApiError(error, assistantMessages.loadError))
    } finally {
      if (mountedRef.current) {
        setLoadingConversation(false)
      }
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true
    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (!open) {
      return
    }
    void loadConversation()
    const focusTimer = window.setTimeout(() => {
      inputRef.current?.focus()
    }, 100)
    return () => {
      window.clearTimeout(focusTimer)
    }
  }, [loadConversation, open])

  useEffect(() => {
    if (open) {
      scrollToBottom()
    }
  }, [messages, sending, open, scrollToBottom])

  const sendMessage = useCallback(
    async (messageText: string) => {
      const trimmed = messageText.trim()
      if (!trimmed || sending) {
        return
      }

      const optimisticUserMessage: ConversationMessage = {
        id: `pending-user-${Date.now()}`,
        role: 'USER',
        content: trimmed,
        createdAt: new Date().toISOString(),
      }

      setChatError(null)
      setInputValue('')
      setMessages((current) => [...current, optimisticUserMessage])
      setSending(true)

      try {
        const response = await assistantApi.sendMessage({
          message: trimmed,
          conversationId,
        })
        if (!mountedRef.current) {
          return
        }

        const assistantMessage: ConversationMessage = {
          id: `assistant-${response.conversationId}-${Date.now()}`,
          role: 'ASSISTANT',
          content: response.response,
          createdAt: new Date().toISOString(),
        }

        setConversationId(response.conversationId)
        setMessages((current) => [...current, assistantMessage])
      } catch (error) {
        if (!mountedRef.current) {
          return
        }
        setInputValue(trimmed)
        setMessages((current) => current.filter((message) => message.id !== optimisticUserMessage.id))
        setChatError(resolveApiError(error, assistantMessages.chatError))
      } finally {
        if (mountedRef.current) {
          setSending(false)
        }
      }
    },
    [conversationId, sending],
  )

  const handleSubmit = () => {
    void sendMessage(inputValue)
  }

  const handleKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      handleSubmit()
    }
  }

  const displayableMessages = messages.filter(isDisplayableMessage)
  const showSuggestedPrompts = !loadingConversation && !conversationError && displayableMessages.length === 0

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
        right: { xs: 16, sm: 24 },
        bottom: { xs: 88, sm: 96 },
        width: { xs: 'calc(100vw - 32px)', sm: 380 },
        maxWidth: 380,
        height: { xs: 'min(70vh, 520px)', sm: 520 },
        display: 'flex',
        flexDirection: 'column',
        zIndex: (theme) => theme.zIndex.modal,
        borderRadius: 3,
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
          bgcolor: 'background.paper',
        }}
      >
        <Box sx={{ alignItems: 'center', display: 'flex', gap: 1 }}>
          <SmartToyOutlinedIcon color="primary" fontSize="small" />
          <Typography component="h2" variant="subtitle1">
            {assistantMessages.title}
          </Typography>
        </Box>
        <IconButton aria-label={assistantMessages.close} onClick={onClose} size="small">
          <CloseIcon fontSize="small" />
        </IconButton>
      </Box>

      <Box
        sx={{
          flex: 1,
          overflowY: 'auto',
          px: 2,
          py: 2,
          bgcolor: 'background.default',
        }}
      >
        {loadingConversation ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress aria-label={assistantMessages.loadingConversation} size={28} />
          </Box>
        ) : null}

        {conversationError ? (
          <Alert
            action={
              <Button color="inherit" onClick={() => void loadConversation()} size="small">
                {assistantMessages.retry}
              </Button>
            }
            severity="error"
            sx={{ mb: 2 }}
          >
            {conversationError}
          </Alert>
        ) : null}

        {!loadingConversation && !conversationError ? (
          <>
            {showSuggestedPrompts ? (
              <Box sx={{ mb: 2 }}>
                <Typography color="text.secondary" sx={{ mb: 1.5 }} variant="body2">
                  {assistantMessages.emptyHint}
                </Typography>
                <Stack spacing={1}>
                  {assistantMessages.suggestedPrompts.map((prompt) => (
                    <Button
                      key={prompt}
                      disabled={sending}
                      onClick={() => void sendMessage(prompt)}
                      size="small"
                      sx={{ justifyContent: 'flex-start', textAlign: 'left' }}
                      variant="outlined"
                    >
                      {prompt}
                    </Button>
                  ))}
                </Stack>
              </Box>
            ) : null}

            {displayableMessages.map((message) => (
              <AssistantMessageBubble key={message.id} message={message} />
            ))}

            {sending ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, py: 1 }}>
                <CircularProgress aria-label={assistantMessages.thinking} size={18} />
                <Typography color="text.secondary" variant="body2">
                  {assistantMessages.thinking}
                </Typography>
              </Box>
            ) : null}

            <div ref={messagesEndRef} />
          </>
        ) : null}
      </Box>

      {chatError ? (
        <Alert
          action={
            <Button color="inherit" onClick={handleSubmit} size="small">
              {assistantMessages.retry}
            </Button>
          }
          severity="error"
          sx={{ mx: 2, mb: 1 }}
        >
          {chatError}
        </Alert>
      ) : null}

      <Box
        sx={{
          display: 'flex',
          alignItems: 'flex-end',
          gap: 1,
          px: 2,
          py: 1.5,
          borderTop: 1,
          borderColor: 'divider',
          bgcolor: 'background.paper',
        }}
      >
        <TextField
          disabled={loadingConversation || Boolean(conversationError) || sending}
          fullWidth
          inputRef={inputRef}
          maxRows={4}
          multiline
          onChange={(event) => setInputValue(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={assistantMessages.inputPlaceholder}
          size="small"
          value={inputValue}
        />
        <IconButton
          aria-label={assistantMessages.send}
          color="primary"
          disabled={!inputValue.trim() || loadingConversation || Boolean(conversationError) || sending}
          onClick={handleSubmit}
        >
          <SendIcon />
        </IconButton>
      </Box>
    </Paper>
  )
}
