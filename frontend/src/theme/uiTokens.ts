import type { Theme } from '@mui/material/styles'
import { alpha } from '@mui/material/styles'

/** Shared page section spacing below headers and between major blocks. */
export const pageSectionSpacing = {
  mb: 3,
} as const

/** Consistent card content padding for list and metric surfaces. */
export const cardContentPadding = {
  p: 2.5,
  '&:last-child': { pb: 2.5 },
} as const

/**
 * Premium metric card hover — soft gradient, elevation, icon glow.
 * No border accent lines or layout shift.
 */
export function metricCardHoverSx(theme: Theme, interactive = true) {
  if (!interactive) {
    return { height: '100%' }
  }

  return {
    height: '100%',
    transition: theme.transitions.create(['box-shadow', 'background'], {
      duration: theme.transitions.duration.short,
      easing: theme.transitions.easing.easeInOut,
    }),
    '&:hover': {
      boxShadow: theme.shadows[2],
      background: `linear-gradient(145deg, ${theme.palette.background.paper} 0%, ${alpha(theme.palette.primary.main, 0.045)} 100%)`,
      '& .metric-icon': {
        color: theme.palette.primary.main,
        filter: `drop-shadow(0 0 8px ${alpha(theme.palette.primary.main, 0.35)})`,
      },
    },
  }
}

/**
 * Interactive outlined card hover for grids (featured items, leaderboard cards).
 */
export function interactiveCardHoverSx(theme: Theme) {
  return {
    transition: theme.transitions.create(['box-shadow', 'background'], {
      duration: theme.transitions.duration.short,
      easing: theme.transitions.easing.easeInOut,
    }),
    '&:hover': {
      boxShadow: theme.shadows[2],
      background: `linear-gradient(145deg, ${theme.palette.background.paper} 0%, ${alpha(theme.palette.primary.main, 0.04)} 100%)`,
    },
  }
}

/** Muted empty-state panel for lists and tables. */
export const emptyStateSx = {
  px: 3,
  py: 4,
  textAlign: 'center',
} as const
