import { Card, CardContent, Typography } from '@mui/material'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeDescriptionCardProps {
  description: string
}

export function InitiativeDescriptionCard({ description }: InitiativeDescriptionCardProps) {
  return (
    <Card sx={{ mb: 2 }} variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {INITIATIVE_MESSAGES.about}
        </Typography>
        <Typography sx={{ whiteSpace: 'pre-wrap' }} variant="body1">
          {description}
        </Typography>
      </CardContent>
    </Card>
  )
}
