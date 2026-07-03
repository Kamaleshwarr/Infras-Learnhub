import OpenInNewOutlinedIcon from '@mui/icons-material/OpenInNewOutlined'
import {
  Box,
  Card,
  CardContent,
  Chip,
  Link,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material'
import type { RoadmapResource } from '../../types/roadmap'
import { LEARN_MESSAGES } from './learnMessages'

function formatResourceType(type: string) {
  return type
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function ResourceList({ resources, title }: { resources: RoadmapResource[]; title: string }) {
  if (resources.length === 0) {
    return null
  }

  return (
    <Stack spacing={1}>
      <Typography variant="subtitle2">{title}</Typography>
      <List dense disablePadding>
        {resources.map((resource) => (
          <ListItem disableGutters key={resource.slug} sx={{ alignItems: 'flex-start', py: 0.75 }}>
            <ListItemText
              primary={
                <Link href={resource.url} rel="noopener noreferrer" target="_blank">
                  {resource.title}
                  <OpenInNewOutlinedIcon fontSize="inherit" sx={{ ml: 0.5, verticalAlign: 'text-bottom' }} />
                </Link>
              }
              secondary={
                <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', mt: 0.5 }}>
                  <Typography color="text.secondary" component="span" variant="caption">
                    {resource.provider}
                  </Typography>
                  <Typography color="text.secondary" component="span" variant="caption">
                    {formatResourceType(resource.type)}
                  </Typography>
                  {resource.freePaid ? (
                    <Chip label={resource.freePaid} size="small" variant="outlined" />
                  ) : null}
                </Stack>
              }
            />
          </ListItem>
        ))}
      </List>
      <Typography color="text.secondary" variant="caption">
        {LEARN_MESSAGES.roadmapExternalLink}
      </Typography>
    </Stack>
  )
}

interface RoadmapStageCardProps {
  stageNumber: number
  totalStages: number
  isRecommended: boolean
  isNext: boolean
  stage: {
    order: number
    slug: string
    title: string
    description: string
    estimatedEffort: string
    notes?: string | null
    learningResources: RoadmapResource[]
    practiceResources: RoadmapResource[]
  }
}

export function RoadmapStageCard({
  stageNumber,
  totalStages,
  isRecommended,
  isNext,
  stage,
}: RoadmapStageCardProps) {
  return (
    <Card id={`stage-${stage.slug}`} variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ alignItems: { sm: 'center' } }}>
            <Chip color="primary" label={`Stage ${stageNumber} of ${totalStages}`} size="small" />
            {isRecommended ? <Chip color="success" label={LEARN_MESSAGES.roadmapCurrentStage} size="small" /> : null}
            {isNext ? <Chip color="info" label={LEARN_MESSAGES.roadmapNextStage} size="small" variant="outlined" /> : null}
            <Box sx={{ flexGrow: 1 }} />
            <Typography color="text.secondary" variant="body2">
              {stage.estimatedEffort}
            </Typography>
          </Stack>

          <Stack spacing={1}>
            <Typography variant="h6">{stage.title}</Typography>
            <Typography>{stage.description}</Typography>
            {stage.notes ? (
              <Typography color="text.secondary" variant="body2">
                {stage.notes}
              </Typography>
            ) : null}
          </Stack>

          <ResourceList resources={stage.learningResources} title={LEARN_MESSAGES.roadmapLearningResources} />
          <ResourceList resources={stage.practiceResources} title={LEARN_MESSAGES.roadmapPracticeResources} />
        </Stack>
      </CardContent>
    </Card>
  )
}
