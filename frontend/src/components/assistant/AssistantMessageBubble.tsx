import { Box, Chip, Typography } from '@mui/material'
import type { ConversationMessage } from '../../types/assistant'
import { longTextWrapSx } from '../common/textStyles'

interface AssistantMessageBubbleProps {
  message: ConversationMessage
}

export function AssistantMessageBubble({ message }: AssistantMessageBubbleProps) {
  const isUser = message.role === 'USER'

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: isUser ? 'flex-end' : 'flex-start',
        mb: 1.5,
      }}
    >
      <Chip
        label={isUser ? 'You' : 'Assistant'}
        size="small"
        sx={{
          mb: 0.5,
          height: 20,
          fontSize: '0.7rem',
          bgcolor: isUser ? 'primary.main' : 'grey.300',
          color: isUser ? 'primary.contrastText' : 'text.primary',
        }}
      />
      <Box
        sx={{
          maxWidth: '85%',
          px: 1.5,
          py: 1,
          borderRadius: 2,
          bgcolor: isUser ? 'primary.light' : 'grey.100',
          color: isUser ? 'primary.contrastText' : 'text.primary',
          ...longTextWrapSx,
        }}
      >
        <Typography component="p" sx={{ m: 0, whiteSpace: 'pre-wrap' }} variant="body2">
          {message.content}
        </Typography>
      </Box>
      {message.createdAt ? (
        <Typography color="text.secondary" sx={{ mt: 0.5, px: 0.5 }} variant="caption">
          {formatMessageTimestamp(message.createdAt)}
        </Typography>
      ) : null}
    </Box>
  )
}

function formatMessageTimestamp(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}
