import type { ReactNode } from 'react'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import {
  Box,
  Button,
  LinearProgress,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { LEARN_MESSAGES } from './learnMessages'
import type { TechnologyProgress } from '../../types/progress'
import type { Roadmap } from '../../types/roadmap'
import { getRemainingEffortSummary, summarizeStageEfforts } from '../../utils/roadmapEffort'

interface RoadmapHeroProps {
  roadmap: Roadmap
  progress: TechnologyProgress | null
  technologyId: string
  enrolling: boolean
  isRoadmapComplete: boolean
  onStartLearning: () => void
  onContinueLearning: () => void
}

function MetadataItem({ children, ...props }: { children: ReactNode; 'aria-hidden'?: boolean }) {
  return (
    <Typography color="text.secondary" component="span" sx={{ whiteSpace: 'nowrap' }} variant="body2" {...props}>
      {children}
    </Typography>
  )
}

export function RoadmapHero({
  roadmap,
  progress,
  technologyId,
  enrolling,
  isRoadmapComplete,
  onStartLearning,
  onContinueLearning,
}: RoadmapHeroProps) {
  const hasEnrollment = progress != null
  const totalEffort = summarizeStageEfforts(roadmap.stages.map((stage) => stage.estimatedEffort))
  const remaining = hasEnrollment
    ? getRemainingEffortSummary(roadmap.stages, progress.completedStageIds)
    : null

  return (
    <Box
      sx={{
        bgcolor: 'background.paper',
        border: 1,
        borderColor: 'divider',
        borderRadius: 2,
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          background: (theme) =>
            `linear-gradient(135deg, ${theme.palette.primary.main}14 0%, ${theme.palette.background.paper} 55%)`,
          px: { xs: 2, sm: 3 },
          py: { xs: 2, sm: 2.5 },
        }}
      >
        <Button
          component={RouterLink}
          size="small"
          startIcon={<ArrowBackIcon fontSize="small" />}
          sx={{ alignSelf: 'flex-start', mb: 1.5, ml: -0.5, px: 0.5 }}
          to={`/learn/technologies/${technologyId}`}
          variant="text"
        >
          {LEARN_MESSAGES.roadmapBackToTechnology}
        </Button>

        <Typography component="h1" sx={{ fontWeight: 800, lineHeight: 1.2, mb: 0.5 }} variant="h4">
          {roadmap.technologyName} {LEARN_MESSAGES.roadmapTitle}
        </Typography>

        {!hasEnrollment && roadmap.description ? (
          <Typography color="text.secondary" sx={{ mb: 1.5, maxWidth: 720 }} variant="body2">
            {roadmap.description}
          </Typography>
        ) : null}

        {hasEnrollment ? (
          <Stack spacing={2} sx={{ mt: 1.5 }}>
            <Box>
              <Stack direction="row" spacing={2} sx={{ alignItems: 'baseline', justifyContent: 'space-between', mb: 1 }}>
                <Typography component="p" sx={{ fontWeight: 700 }} variant="h5">
                  {progress.progressPercent}% {LEARN_MESSAGES.roadmapHeroComplete}
                </Typography>
                <Typography aria-live="polite" color="text.secondary" variant="body2">
                  {progress.completedStageCount} of {roadmap.stageCount} {LEARN_MESSAGES.roadmapHeroStagesCompleted}
                </Typography>
              </Stack>
              <LinearProgress
                aria-label={LEARN_MESSAGES.progressBarLabel}
                sx={{
                  '& .MuiLinearProgress-bar': { borderRadius: 6 },
                  bgcolor: 'action.hover',
                  borderRadius: 6,
                  height: 12,
                }}
                value={progress.progressPercent}
                variant="determinate"
              />
            </Box>

            {isRoadmapComplete ? (
              <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start' }}>
                <CheckCircleIcon aria-hidden color="success" />
                <Typography color="success.main" sx={{ fontWeight: 600 }} variant="body1">
                  {LEARN_MESSAGES.progressEnrollmentComplete}
                </Typography>
              </Stack>
            ) : (
              <Stack
                direction={{ sm: 'row' }}
                spacing={{ xs: 2, sm: 4 }}
                sx={{ alignItems: { sm: 'flex-start' } }}
              >
                <Box>
                  <Typography color="text.secondary" sx={{ display: 'block', mb: 0.25 }} variant="overline">
                    {LEARN_MESSAGES.roadmapHeroCurrentStage}
                  </Typography>
                  <Typography sx={{ fontWeight: 700 }} variant="h6">
                    {progress.currentStageTitle ?? '—'}
                  </Typography>
                </Box>
                {progress.nextStageTitle ? (
                  <Box>
                    <Typography color="text.secondary" sx={{ display: 'block', mb: 0.25 }} variant="overline">
                      {LEARN_MESSAGES.roadmapHeroNextStage}
                    </Typography>
                    <Typography sx={{ fontWeight: 600 }} variant="subtitle1">
                      {progress.nextStageTitle}
                    </Typography>
                  </Box>
                ) : null}
              </Stack>
            )}

            {!isRoadmapComplete ? (
              <Button onClick={onContinueLearning} size="large" sx={{ alignSelf: 'flex-start' }} variant="contained">
                {LEARN_MESSAGES.progressContinueLearning}
              </Button>
            ) : null}
          </Stack>
        ) : (
          <Stack spacing={2} sx={{ mt: 1.5 }}>
            <Typography color="text.secondary" variant="body2">
              {LEARN_MESSAGES.viewRoadmapHelper}
            </Typography>
            <Button disabled={enrolling} onClick={onStartLearning} size="large" sx={{ alignSelf: 'flex-start' }} variant="contained">
              {LEARN_MESSAGES.progressStartLearning}
            </Button>
          </Stack>
        )}
      </Box>

      <Box
        sx={{
          borderTop: 1,
          borderColor: 'divider',
          display: 'flex',
          flexWrap: 'wrap',
          gap: { xs: 1, sm: 1.5 },
          px: { xs: 2, sm: 3 },
          py: 1.25,
        }}
      >
        <MetadataItem>
          {roadmap.stageCount} {LEARN_MESSAGES.roadmapHeroStagesLabel}
        </MetadataItem>
        <MetadataItem aria-hidden>·</MetadataItem>
        <MetadataItem>{totalEffort}</MetadataItem>
        {remaining && remaining.stagesLeft > 0 ? (
          <>
            <MetadataItem aria-hidden>·</MetadataItem>
            <MetadataItem>
              {remaining.stagesLeft} {LEARN_MESSAGES.roadmapHeroStagesLeft}
            </MetadataItem>
            <MetadataItem aria-hidden>·</MetadataItem>
            <MetadataItem>
              {LEARN_MESSAGES.roadmapHeroRemaining} {remaining.effortLabel}
            </MetadataItem>
          </>
        ) : null}
        <MetadataItem>
          <Box component="span" sx={{ color: 'text.disabled' }}>
            {LEARN_MESSAGES.roadmapHeroVersion} {roadmap.version}
          </Box>
        </MetadataItem>
      </Box>
    </Box>
  )
}
