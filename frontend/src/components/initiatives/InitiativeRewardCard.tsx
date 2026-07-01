import { Card, CardContent, Typography } from '@mui/material'
import { longTextWrapSx } from '../common/textStyles'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeRewardCardProps {
  rewardDescription: string
}

export function InitiativeRewardCard({ rewardDescription }: InitiativeRewardCardProps) {
  return (
    <Card sx={{ mb: 2, minWidth: 0 }} variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {INITIATIVE_MESSAGES.reward}
        </Typography>
        <Typography sx={{ whiteSpace: 'pre-wrap', ...longTextWrapSx }} variant="body1">
          {rewardDescription}
        </Typography>
      </CardContent>
    </Card>
  )
}
