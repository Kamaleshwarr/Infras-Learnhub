import { Card, CardContent, Typography } from '@mui/material'
import { cardContentPadding, emptyStateSx } from '../../theme/uiTokens'

interface EmptyStatePanelProps {
  message: string
}

/** Centered muted empty state for list and table surfaces. */
export function EmptyStatePanel({ message }: EmptyStatePanelProps) {
  return (
    <Card variant="outlined">
      <CardContent sx={{ ...cardContentPadding, ...emptyStateSx, py: 5 }}>
        <Typography color="text.secondary" variant="body2">
          {message}
        </Typography>
      </CardContent>
    </Card>
  )
}
