import { Alert, Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { RelatedProjectSummary } from '../../types/learn'
import { LEARN_MESSAGES } from './learnMessages'

interface RelatedOrganizationProjectsCardProps {
  projects: RelatedProjectSummary[]
}

export function RelatedOrganizationProjectsCard({ projects }: RelatedOrganizationProjectsCardProps) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <div>
            <Typography variant="h6">{LEARN_MESSAGES.relatedProjectsTitle}</Typography>
            <Typography color="text.secondary" variant="body2">
              {LEARN_MESSAGES.relatedProjectsDescription}
            </Typography>
          </div>
          {projects.length === 0 ? (
            <Alert severity="info">{LEARN_MESSAGES.relatedProjectsEmpty}</Alert>
          ) : (
            <Stack spacing={1}>
              {projects.map((project) => (
                <Button
                  component={RouterLink}
                  key={project.id}
                  sx={{ justifyContent: 'flex-start' }}
                  to={`/projects/${project.id}`}
                  variant="text"
                >
                  {project.name}
                </Button>
              ))}
            </Stack>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}
