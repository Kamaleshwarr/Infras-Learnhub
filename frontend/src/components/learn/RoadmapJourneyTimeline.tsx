import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked'
import { Box, Step, StepConnector, stepConnectorClasses, StepLabel, Stepper, Typography } from '@mui/material'
import type { StepIconProps } from '@mui/material/StepIcon'
import { styled } from '@mui/material/styles'
import type { TechnologyProgress } from '../../types/progress'
import type { RoadmapStage } from '../../types/roadmap'
import { LEARN_MESSAGES } from './learnMessages'

const JourneyConnector = styled(StepConnector)(({ theme }) => ({
  [`&.${stepConnectorClasses.active}`]: {
    [`& .${stepConnectorClasses.line}`]: {
      borderColor: theme.palette.primary.main,
    },
  },
  [`&.${stepConnectorClasses.completed}`]: {
    [`& .${stepConnectorClasses.line}`]: {
      borderColor: theme.palette.success.main,
    },
  },
  [`& .${stepConnectorClasses.line}`]: {
    borderColor: theme.palette.divider,
    borderTopWidth: 3,
    borderRadius: 1,
  },
}))

function JourneyStepIcon({ active, completed }: StepIconProps) {
  if (completed) {
    return <CheckCircleIcon aria-label={LEARN_MESSAGES.progressStageCompleted} color="success" fontSize="small" />
  }
  if (active) {
    return (
      <Box
        aria-label={LEARN_MESSAGES.roadmapHeroCurrentStage}
        sx={{
          alignItems: 'center',
          bgcolor: 'primary.main',
          borderRadius: '50%',
          display: 'flex',
          height: 22,
          justifyContent: 'center',
          width: 22,
        }}
      >
        <Box sx={{ bgcolor: 'common.white', borderRadius: '50%', height: 8, width: 8 }} />
      </Box>
    )
  }
  return <RadioButtonUncheckedIcon aria-hidden color="disabled" fontSize="small" />
}

interface RoadmapJourneyTimelineProps {
  stages: RoadmapStage[]
  progress: TechnologyProgress
  activeStep: number
  isMobile: boolean
  isRoadmapComplete: boolean
}

export function RoadmapJourneyTimeline({
  stages,
  progress,
  activeStep,
  isMobile,
  isRoadmapComplete,
}: RoadmapJourneyTimelineProps) {
  return (
    <Box
      aria-label={LEARN_MESSAGES.roadmapJourneyTimeline}
      sx={{
        bgcolor: 'background.paper',
        border: 1,
        borderColor: 'divider',
        borderRadius: 2,
        px: { xs: 1.5, sm: 2 },
        py: { xs: 1.5, sm: 2 },
      }}
    >
      <Typography component="h2" sx={{ fontWeight: 700, mb: 1.5 }} variant="subtitle2">
        {LEARN_MESSAGES.roadmapJourneyTimeline}
      </Typography>
      <Stepper
        activeStep={activeStep}
        alternativeLabel={!isMobile}
        connector={<JourneyConnector />}
        nonLinear
        orientation={isMobile ? 'vertical' : 'horizontal'}
      >
        {stages.map((stage, index) => {
          const completed = progress.completedStageOrders.includes(stage.order)
          const isCurrentStep = index === activeStep && !isRoadmapComplete

          return (
            <Step completed={completed} key={stage.slug}>
              <StepLabel
                onClick={() => {
                  document.getElementById(`stage-${stage.slug}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                }}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault()
                    document.getElementById(`stage-${stage.slug}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                  }
                }}
                slots={{ stepIcon: JourneyStepIcon }}
                sx={{
                  '& .MuiStepLabel-label': {
                    color: completed ? 'success.main' : isCurrentStep ? 'primary.main' : 'text.secondary',
                    cursor: 'pointer',
                    fontWeight: isCurrentStep ? 700 : completed ? 600 : 400,
                    mt: isMobile ? 0 : 0.75,
                  },
                }}
                tabIndex={0}
              >
                {stage.title}
              </StepLabel>
            </Step>
          )
        })}
      </Stepper>
    </Box>
  )
}
