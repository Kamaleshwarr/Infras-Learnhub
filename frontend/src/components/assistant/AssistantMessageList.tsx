import { useEffect, useRef } from 'react'
import { Box, CircularProgress } from '@mui/material'
import type { AssistantUiMessage } from '../../types/assistant'
import { AssistantEmptyState, AssistantMessageBubble } from './AssistantMessageBubble'
import { assistantMessages } from './assistantMessages'

interface AssistantMessageListProps {
  messages: AssistantUiMessage[]
  loading: boolean
  onExampleClick: (example: string) => void
}

export function AssistantMessageList({ messages, loading, onExampleClick }: AssistantMessageListProps) {
  const bottomRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView?.({ behavior: 'smooth' })
  }, [messages, loading])

  if (loading && messages.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flex: 1, py: 4 }}>
        <CircularProgress aria-label={assistantMessages.loadingHistory} size={28} />
      </Box>
    )
  }

  if (messages.length === 0) {
    return <AssistantEmptyState onExampleClick={onExampleClick} />
  }

  return (
    <Box
      aria-live="polite"
      sx={{
        flex: 1,
        overflowY: 'auto',
        px: 2,
        py: 2,
      }}
    >
      {messages.map((message) => (
        <AssistantMessageBubble key={message.id} message={message} />
      ))}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'flex-start', mb: 1.5 }}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              px: 1.5,
              py: 1,
              borderRadius: 2,
              bgcolor: 'grey.100',
            }}
          >
            <CircularProgress aria-label={assistantMessages.thinking} size={16} />
          </Box>
        </Box>
      ) : null}
      <div ref={bottomRef} />
    </Box>
  )
}
