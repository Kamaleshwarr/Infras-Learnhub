import { Card, CardContent, Typography } from '@mui/material'
import { longTextWrapSx } from '../common/textStyles'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeDescriptionCardProps {
  description: string
}

export function InitiativeDescriptionCard({ description }: InitiativeDescriptionCardProps) {
  return (
    <Card sx={{ mb: 2, minWidth: 0 }} variant="outlined">
      <CardContent>
        <Typography gutterBottom variant="h6">
          {INITIATIVE_MESSAGES.about}
        </Typography>
        <Typography sx={{ whiteSpace: 'pre-wrap', ...longTextWrapSx }} variant="body1">
          {description}
        </Typography>
      </CardContent>
    </Card>
  )
}
