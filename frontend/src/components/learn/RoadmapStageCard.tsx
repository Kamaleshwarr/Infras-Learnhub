import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import OpenInNewOutlinedIcon from '@mui/icons-material/OpenInNewOutlined'
import PlayCircleOutlinedIcon from '@mui/icons-material/PlayCircleOutlined'
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked'
import {
  alpha,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Link,
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

function ResourceGroup({ resources, title }: { resources: RoadmapResource[]; title: string }) {
  if (resources.length === 0) {
    return null
  }

  return (
    <Box
      sx={{
        bgcolor: (theme) => alpha(theme.palette.grey[500], 0.05),
        border: 1,
        borderColor: 'divider',
        borderRadius: 1.5,
        p: { xs: 1.25, sm: 1.5 },
      }}
    >
      <Typography sx={{ fontWeight: 600, mb: 1 }} variant="subtitle2">
        {title}
      </Typography>
      <Stack spacing={0.75}>
        {resources.map((resource) => (
          <Box
            key={resource.slug}
            sx={{
              bgcolor: 'background.paper',
              border: 1,
              borderColor: 'divider',
              borderRadius: 1,
              px: { xs: 1.25, sm: 1.5 },
              py: 1,
            }}
          >
            <Link
              href={resource.url}
              rel="noopener noreferrer"
              sx={{ alignItems: 'center', display: 'inline-flex', fontWeight: 600, gap: 0.5 }}
              target="_blank"
              underline="hover"
              variant="body2"
            >
              {resource.title}
              <OpenInNewOutlinedIcon fontSize="inherit" />
            </Link>
            <Stack
              direction="row"
              spacing={1}
              sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}
            >
              <Typography color="text.secondary" variant="caption">
                {resource.provider}
              </Typography>
              <Typography color="text.secondary" variant="caption">
                ·
              </Typography>
              <Typography color="text.secondary" variant="caption">
                {formatResourceType(resource.type)}
              </Typography>
              {resource.freePaid ? (
                <Chip label={resource.freePaid} size="small" sx={{ height: 18 }} variant="outlined" />
              ) : null}
            </Stack>
          </Box>
        ))}
      </Stack>
    </Box>
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
      sx={(theme) => ({
        bgcolor: isCurrent
          ? alpha(theme.palette.primary.main, 0.05)
          : isCompleted
            ? alpha(theme.palette.success.main, 0.025)
            : 'background.paper',
        borderColor: isCurrent ? 'primary.main' : isCompleted ? 'success.light' : 'divider',
        borderLeft: isCompleted ? `4px solid ${theme.palette.success.main}` : undefined,
        borderWidth: isCurrent ? 2 : 1,
        boxShadow: isCurrent ? theme.shadows[3] : 0,
        mb: 1.5,
        opacity: isUpcoming && !isCurrent ? 0.92 : 1,
        transition: theme.transitions.create(['box-shadow', 'border-color', 'opacity']),
      })}
      variant="outlined"
    >
      <CardContent sx={{ '&:last-child': { pb: 1.75 }, p: { xs: 1.75, sm: 2 } }}>
        <Stack spacing={1.75}>
          <Stack direction={{ sm: 'row' }} spacing={1} sx={{ alignItems: { sm: 'flex-start' }, justifyContent: 'space-between' }}>
            <Stack spacing={0.75} sx={{ flex: 1, minWidth: 0 }}>
              <Stack direction="row" spacing={0.75} sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 0.5 }}>
                {isCompleted ? (
                  <CheckCircleOutlinedIcon aria-hidden color="success" fontSize="small" />
                ) : isCurrent ? (
                  <PlayCircleOutlinedIcon aria-hidden color="primary" fontSize="small" />
                ) : (
                  <RadioButtonUncheckedIcon aria-hidden color="disabled" fontSize="small" />
                )}
                <Typography color="text.secondary" variant="caption">
                  Stage {stageNumber} of {totalStages}
                </Typography>
                {isCompleted ? (
                  <Chip
                    color="success"
                    label={LEARN_MESSAGES.progressStageCompleted}
                    size="small"
                    sx={{ height: 20 }}
                    variant="outlined"
                  />
                ) : null}
                {isCurrent ? (
                  <Chip color="primary" label={LEARN_MESSAGES.roadmapCurrentStage} size="small" sx={{ height: 20 }} />
                ) : null}
                {!isCompleted && isNext ? (
                  <Chip
                    color="info"
                    label={LEARN_MESSAGES.roadmapNextStage}
                    size="small"
                    sx={{ height: 20 }}
                    variant="outlined"
                  />
                ) : null}
              </Stack>

              <Typography
                component="h3"
                sx={{
                  color: isCompleted ? 'text.secondary' : 'text.primary',
                  fontWeight: isCurrent ? 700 : 600,
                  lineHeight: 1.3,
                }}
                variant="subtitle1"
              >
                {stage.title}
              </Typography>
            </Stack>

            <Typography color="text.secondary" sx={{ flexShrink: 0, fontWeight: 500 }} variant="caption">
              {stage.estimatedEffort}
            </Typography>
          </Stack>

          <Typography
            color={isCompleted ? 'text.secondary' : 'text.primary'}
            sx={{ lineHeight: 1.55 }}
            variant="body2"
          >
            {stage.description}
          </Typography>

          {stage.notes ? (
            <Typography color="text.secondary" variant="caption">
              {stage.notes}
            </Typography>
          ) : null}

          <Stack spacing={1.25}>
            <ResourceGroup resources={stage.learningResources} title={LEARN_MESSAGES.roadmapLearningResources} />
            <ResourceGroup resources={stage.practiceResources} title={LEARN_MESSAGES.roadmapPracticeResources} />
          </Stack>

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
