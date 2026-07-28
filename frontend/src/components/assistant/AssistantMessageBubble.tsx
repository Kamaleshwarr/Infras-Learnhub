import { Box, Button, Link, Typography } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import type { AssistantUiMessage } from '../../types/assistant'
import { assistantMessages } from './assistantMessages'

interface AssistantMessageBubbleProps {
  message: AssistantUiMessage
}

function isNavigationMetadata(
  metadata: Record<string, unknown> | undefined,
): metadata is { navigation: { path: string; label: string } } {
  if (!metadata || typeof metadata.navigation !== 'object' || metadata.navigation === null) {
    return false
  }
  const navigation = metadata.navigation as { path?: unknown; label?: unknown }
  return typeof navigation.path === 'string' && typeof navigation.label === 'string'
}

export function AssistantMessageBubble({ message }: AssistantMessageBubbleProps) {
  const navigate = useNavigate()
  const isUser = message.role === 'USER'
  const navigation = isNavigationMetadata(message.metadata) ? message.metadata.navigation : null

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isUser ? 'flex-end' : 'flex-start',
        mb: 1.5,
      }}
    >
      <Box
        sx={{
          maxWidth: '85%',
          px: 1.5,
          py: 1,
          borderRadius: 2,
          bgcolor: message.error
            ? 'error.light'
            : isUser
              ? 'primary.main'
              : 'grey.100',
          color: message.error ? 'error.contrastText' : isUser ? 'primary.contrastText' : 'text.primary',
        }}
      >
        <Typography
          component="div"
          variant="body2"
          sx={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}
        >
          {message.content}
        </Typography>
        {navigation ? (
          <Button
            color={isUser ? 'inherit' : 'primary'}
            onClick={() => navigate(navigation.path)}
            size="small"
            sx={{ mt: 1, textTransform: 'none' }}
            variant={isUser ? 'outlined' : 'contained'}
          >
            {assistantMessages.navigateTo(navigation.label)}
          </Button>
        ) : null}
        {message.pending ? (
          <Typography color="inherit" sx={{ mt: 0.5, opacity: 0.8 }} variant="caption">
            {assistantMessages.thinking}
          </Typography>
        ) : null}
      </Box>
    </Box>
  )
}

export function AssistantEmptyState({ onExampleClick }: { onExampleClick: (example: string) => void }) {
  return (
    <Box sx={{ px: 2, py: 3, textAlign: 'center' }}>
      <Typography gutterBottom variant="subtitle1">
        {assistantMessages.emptyTitle}
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }} variant="body2">
        {assistantMessages.emptyDescription}
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, alignItems: 'center' }}>
        {assistantMessages.examples.map((example) => (
          <Link
            component="button"
            key={example}
            onClick={() => onExampleClick(example)}
            sx={{ cursor: 'pointer' }}
            type="button"
            underline="hover"
          >
            {example}
          </Link>
        ))}
      </Box>
    </Box>
  )
}
