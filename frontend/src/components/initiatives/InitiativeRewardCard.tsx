import { Card, CardContent, Typography } from '@mui/material'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeRewardCardProps {
  rewardDescription: string
}

export function InitiativeRewardCard({ rewardDescription }: InitiativeRewardCardProps) {
  return (
    <Card sx={{ mb: 2 }} variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {INITIATIVE_MESSAGES.reward}
        </Typography>
        <Typography sx={{ whiteSpace: 'pre-wrap' }} variant="body1">
          {rewardDescription}
        </Typography>
      </CardContent>
    </Card>
  )
}
