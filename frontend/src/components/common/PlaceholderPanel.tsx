import { Card, CardContent, Stack, Typography } from '@mui/material'

interface PlaceholderPanelProps {
  title: string
  items: string[]
}

export function PlaceholderPanel({ title, items }: PlaceholderPanelProps) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {title}
        </Typography>
        <Stack component="ul" spacing={1} sx={{ m: 0, pl: 2.5 }}>
          {items.map((item) => (
            <Typography component="li" key={item} variant="body2">
              {item}
            </Typography>
          ))}
        </Stack>
      </CardContent>
    </Card>
  )
}
