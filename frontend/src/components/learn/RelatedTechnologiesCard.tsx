import { Alert, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { RelatedTechnologySummary } from '../../types/learn'
import { LEARN_MESSAGES } from './learnMessages'

interface RelatedTechnologiesCardProps {
  technologies: RelatedTechnologySummary[]
}

export function RelatedTechnologiesCard({ technologies }: RelatedTechnologiesCardProps) {
  return (
    <Card sx={{ mt: 3 }} variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <div>
            <Typography variant="h6">{LEARN_MESSAGES.relatedTechnologiesTitle}</Typography>
            <Typography color="text.secondary" variant="body2">
              {LEARN_MESSAGES.relatedTechnologiesDescription}
            </Typography>
          </div>
          {technologies.length === 0 ? (
            <Alert severity="info">{LEARN_MESSAGES.relatedTechnologiesEmpty}</Alert>
          ) : (
            <Stack spacing={1}>
              {technologies.map((technology) => (
                <Button
                  component={RouterLink}
                  key={technology.id}
                  sx={{ justifyContent: 'flex-start' }}
                  to={`/learn/technologies/${technology.id}`}
                  variant="text"
                >
                  {technology.name}
                </Button>
              ))}
            </Stack>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}
