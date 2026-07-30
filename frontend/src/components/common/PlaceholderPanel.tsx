import { Card, CardContent, Stack, Typography } from '@mui/material'
import { cardContentPadding } from '../../theme/uiTokens'

interface PlaceholderPanelProps {
  title: string
  items: string[]
}

export function PlaceholderPanel({ title, items }: PlaceholderPanelProps) {
  return (
    <Card variant="outlined">
      <CardContent sx={cardContentPadding}>
        <Typography gutterBottom sx={{ fontWeight: 700 }} variant="h6">
          {title}
        </Typography>
        <Stack component="ul" spacing={1.25} sx={{ m: 0, pl: 2.5 }}>
          {items.map((item) => (
            <Typography color="text.secondary" component="li" key={item} variant="body2">
              {item}
            </Typography>
          ))}
        </Stack>
      </CardContent>
    </Card>
  )
}
