import { Box, Button, Chip, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import type { ConversationMessage } from '../../types/assistant'
import { longTextWrapSx } from '../common/textStyles'
import { buildNavigationButtonLabel } from './assistantNavigation'

interface AssistantMessageBubbleProps {
  message: ConversationMessage
}

export function AssistantMessageBubble({ message }: AssistantMessageBubbleProps) {
  const navigate = useNavigate()
  const isUser = message.role === 'USER'
  const navigation = !isUser ? message.navigation : null

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
      {navigation ? (
        <Button
          onClick={() => navigate(navigation.path)}
          size="small"
          sx={{ mt: 1 }}
          variant="outlined"
        >
          {buildNavigationButtonLabel(navigation.label)}
        </Button>
      ) : null}
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
