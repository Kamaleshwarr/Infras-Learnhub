import MapOutlinedIcon from '@mui/icons-material/MapOutlined'
import { Box, Button, Card, CardContent, Stack, Typography, useTheme } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { LEARN_MESSAGES } from './learnMessages'
import { TechnologyCategoryChip, TechnologyDifficultyChip } from './TechnologyMetaChips'
import type { Technology } from '../../types/learn'
import { cardContentPadding, interactiveCardHoverSx } from '../../theme/uiTokens'

interface FeaturedTechnologyCardProps {
  technology: Technology
}

const descriptionClampSx = {
  display: '-webkit-box',
  WebkitLineClamp: 2,
  WebkitBoxOrient: 'vertical',
  overflow: 'hidden',
  minHeight: '2.75rem',
  lineHeight: 1.375,
} as const

export function FeaturedTechnologyCard({ technology }: FeaturedTechnologyCardProps) {
  const theme = useTheme()
  const description = technology.description?.trim() || technology.shortName

  return (
    <Card
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        cursor: 'pointer',
        ...interactiveCardHoverSx(theme),
      }}
      variant="outlined"
    >
      <CardContent
        sx={{
          ...cardContentPadding,
          display: 'flex',
          flexDirection: 'column',
          flexGrow: 1,
          gap: 1.5,
        }}
      >
        <Box>
          <Typography component="h3" sx={{ fontWeight: 700, lineHeight: 1.25 }} variant="h6">
            {technology.name}
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.25 }} variant="caption">
            {technology.shortName}
          </Typography>
        </Box>

        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap', gap: 1 }}>
          <TechnologyCategoryChip category={technology.category} />
          <TechnologyDifficultyChip difficulty={technology.difficulty} />
        </Stack>

        <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
          {description}
        </Typography>

        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1}
          sx={{ mt: 'auto', pt: 0.5 }}
        >
          <Button
            component={RouterLink}
            fullWidth
            size="small"
            startIcon={<MapOutlinedIcon />}
            to={`/learn/technologies/${technology.id}/roadmap`}
            variant="contained"
          >
            {LEARN_MESSAGES.viewRoadmap}
          </Button>
          <Button
            component={RouterLink}
            fullWidth
            size="small"
            to={`/learn/technologies/${technology.id}`}
            variant="outlined"
          >
            {LEARN_MESSAGES.homeViewDetails}
          </Button>
        </Stack>
      </CardContent>
    </Card>
  )
}
