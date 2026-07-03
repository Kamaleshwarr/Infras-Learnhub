import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import OpenInNewOutlinedIcon from '@mui/icons-material/OpenInNewOutlined'
import {
  Box,
  Button,
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
  isNext: boolean
  isCompleted?: boolean
  isCurrent?: boolean
  isUpcoming?: boolean
  canComplete?: boolean
  completing?: boolean
  onCompleteStage?: () => void
  stage: {
    id: string
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
  isNext,
  isCompleted = false,
  isCurrent = false,
  isUpcoming = false,
  canComplete = false,
  completing = false,
  onCompleteStage,
  stage,
}: RoadmapStageCardProps) {
  return (
    <Card
      id={`stage-${stage.slug}`}
      sx={{
        borderColor: isCurrent ? 'primary.main' : isCompleted ? 'success.light' : 'divider',
        borderWidth: isCurrent ? 2 : 1,
        opacity: isUpcoming && !isCurrent ? 0.92 : 1,
      }}
      variant="outlined"
    >
      <CardContent>
        <Stack spacing={2}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ alignItems: { sm: 'center' } }}>
            <Chip color="primary" label={`Stage ${stageNumber} of ${totalStages}`} size="small" />
            {isCompleted ? (
              <Chip
                color="success"
                icon={<CheckCircleOutlinedIcon />}
                label={LEARN_MESSAGES.progressStageCompleted}
                size="small"
              />
            ) : null}
            {isCurrent ? <Chip color="success" label={LEARN_MESSAGES.roadmapCurrentStage} size="small" /> : null}
            {!isCompleted && isNext ? (
              <Chip color="info" label={LEARN_MESSAGES.roadmapNextStage} size="small" variant="outlined" />
            ) : null}
            <Box sx={{ flexGrow: 1 }} />
            <Typography color="text.secondary" variant="body2">
              {stage.estimatedEffort}
            </Typography>
          </Stack>

          <Stack spacing={1}>
            <Typography sx={{ fontWeight: isCurrent ? 700 : 600 }} variant="h6">
              {stage.title}
            </Typography>
            <Typography color={isCompleted ? 'text.secondary' : 'text.primary'}>{stage.description}</Typography>
            {stage.notes ? (
              <Typography color="text.secondary" variant="body2">
                {stage.notes}
              </Typography>
            ) : null}
          </Stack>

          <ResourceList resources={stage.learningResources} title={LEARN_MESSAGES.roadmapLearningResources} />
          <ResourceList resources={stage.practiceResources} title={LEARN_MESSAGES.roadmapPracticeResources} />

          {canComplete && onCompleteStage ? (
            <Button disabled={completing} onClick={onCompleteStage} variant="contained">
              {LEARN_MESSAGES.progressCompleteStage}
            </Button>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
