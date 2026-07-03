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
        bgcolor: (theme) => alpha(theme.palette.grey[500], 0.06),
        border: 1,
        borderColor: 'divider',
        borderRadius: 2,
        p: { xs: 1.5, sm: 2 },
      }}
    >
      <Typography sx={{ fontWeight: 600, mb: 1.5 }} variant="subtitle2">
        {title}
      </Typography>
      <Stack spacing={1}>
        {resources.map((resource) => (
          <Card
            key={resource.slug}
            sx={{
              bgcolor: 'background.paper',
              boxShadow: 'none',
            }}
            variant="outlined"
          >
            <CardContent sx={{ '&:last-child': { pb: 1.5 }, px: { xs: 1.5, sm: 2 }, py: 1.5 }}>
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
                sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 0.5, mt: 0.75 }}
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
                  <Chip label={resource.freePaid} size="small" sx={{ height: 20 }} variant="outlined" />
                ) : null}
              </Stack>
            </CardContent>
          </Card>
        ))}
      </Stack>
      <Typography color="text.secondary" sx={{ display: 'block', mt: 1 }} variant="caption">
        {LEARN_MESSAGES.roadmapExternalLink}
      </Typography>
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
          ? alpha(theme.palette.primary.main, 0.04)
          : isCompleted
            ? alpha(theme.palette.success.main, 0.03)
            : 'background.paper',
        borderColor: isCurrent ? 'primary.main' : isCompleted ? 'success.light' : 'divider',
        borderLeft: isCompleted ? `4px solid ${theme.palette.success.main}` : undefined,
        borderWidth: isCurrent ? 2 : 1,
        boxShadow: isCurrent ? theme.shadows[2] : 0,
        opacity: isUpcoming && !isCurrent ? 0.9 : 1,
        transition: theme.transitions.create(['box-shadow', 'border-color', 'opacity']),
      })}
      variant="outlined"
    >
      <CardContent sx={{ '&:last-child': { pb: { xs: 2, sm: 2.5 } }, p: { xs: 2, sm: 2.5 } }}>
        <Stack spacing={2.5}>
          <Stack direction={{ sm: 'row' }} spacing={1.5} sx={{ alignItems: { sm: 'flex-start' }, justifyContent: 'space-between' }}>
            <Stack spacing={1} sx={{ flex: 1, minWidth: 0 }}>
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', gap: 0.75 }}>
                {isCompleted ? (
                  <CheckCircleOutlinedIcon color="success" fontSize="small" />
                ) : isCurrent ? (
                  <PlayCircleOutlinedIcon color="primary" fontSize="small" />
                ) : (
                  <RadioButtonUncheckedIcon color="disabled" fontSize="small" />
                )}
                <Typography color="text.secondary" variant="overline">
                  Stage {stageNumber} of {totalStages}
                </Typography>
                {isCompleted ? (
                  <Chip
                    color="success"
                    label={LEARN_MESSAGES.progressStageCompleted}
                    size="small"
                    sx={{ height: 22 }}
                    variant="outlined"
                  />
                ) : null}
                {isCurrent ? (
                  <Chip color="primary" label={LEARN_MESSAGES.roadmapCurrentStage} size="small" sx={{ height: 22 }} />
                ) : null}
                {!isCompleted && isNext ? (
                  <Chip
                    color="info"
                    label={LEARN_MESSAGES.roadmapNextStage}
                    size="small"
                    sx={{ height: 22 }}
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
                variant="h6"
              >
                {stage.title}
              </Typography>
            </Stack>

            <Typography
              color="text.secondary"
              sx={{ flexShrink: 0, fontWeight: 500, whiteSpace: 'nowrap' }}
              variant="body2"
            >
              {stage.estimatedEffort}
            </Typography>
          </Stack>

          <Typography
            color={isCompleted ? 'text.secondary' : 'text.primary'}
            sx={{ lineHeight: 1.6 }}
            variant="body2"
          >
            {stage.description}
          </Typography>

          {stage.notes ? (
            <Typography color="text.secondary" sx={{ fontStyle: 'italic' }} variant="body2">
              {stage.notes}
            </Typography>
          ) : null}

          <Stack spacing={2}>
            <ResourceGroup resources={stage.learningResources} title={LEARN_MESSAGES.roadmapLearningResources} />
            <ResourceGroup resources={stage.practiceResources} title={LEARN_MESSAGES.roadmapPracticeResources} />
          </Stack>

          {canComplete && onCompleteStage ? (
            <Box>
              <Button disabled={completing} onClick={onCompleteStage} size="large" variant="contained">
                {LEARN_MESSAGES.progressCompleteStage}
              </Button>
            </Box>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
