import PlayArrowOutlinedIcon from '@mui/icons-material/PlayArrowOutlined'
import {
  Box,
  Button,
  Card,
  CardContent,
  LinearProgress,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { LEARN_MESSAGES } from './learnMessages'
import type { ContinueLearning } from '../../types/progress'

interface ContinueLearningCardProps {
  continueLearning: ContinueLearning
}

export function ContinueLearningCard({ continueLearning }: ContinueLearningCardProps) {
  const roadmapHref = `/learn/technologies/${continueLearning.technologyId}/roadmap#stage-${continueLearning.currentStageOrder}`

  return (
    <Card
      sx={{
        border: 1,
        borderColor: 'primary.light',
        mb: 3,
      }}
      variant="outlined"
    >
      <CardContent sx={{ py: 2.5 }}>
        <Stack spacing={2}>
          <Box>
            <Typography component="h2" sx={{ fontWeight: 700 }} variant="h6">
              {LEARN_MESSAGES.homeContinueLearningTitle}
            </Typography>
            <Typography color="text.secondary" variant="body2">
              {LEARN_MESSAGES.homeContinueLearningDescription}
            </Typography>
          </Box>

          <Stack
            direction={{ xs: 'column', md: 'row' }}
            spacing={2}
            sx={{ alignItems: { md: 'center' }, justifyContent: 'space-between' }}
          >
            <Box sx={{ flexGrow: 1, minWidth: 0 }}>
              <Typography sx={{ fontWeight: 600 }} variant="subtitle1">
                {continueLearning.technologyName}
              </Typography>
              <Typography color="text.secondary" variant="body2">
                {LEARN_MESSAGES.homeContinueLearningCurrentStage}: {continueLearning.currentStageTitle}
              </Typography>
              <Stack spacing={0.5} sx={{ mt: 1.5, maxWidth: 360 }}>
                <Typography color="text.secondary" variant="caption">
                  {LEARN_MESSAGES.homeContinueLearningProgress}: {continueLearning.progressPercent}%
                </Typography>
                <LinearProgress
                  aria-label={LEARN_MESSAGES.progressBarLabel}
                  value={continueLearning.progressPercent}
                  variant="determinate"
                />
              </Stack>
            </Box>

            <Button
              component={RouterLink}
              startIcon={<PlayArrowOutlinedIcon />}
              to={roadmapHref}
              variant="contained"
            >
              {LEARN_MESSAGES.homeContinueLearningButton}
            </Button>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  )
}
