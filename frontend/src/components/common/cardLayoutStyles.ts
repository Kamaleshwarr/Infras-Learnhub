/** Shared flex-column card layout for equal-height grid rows with bottom-aligned actions. */
export const flexCardSx = {
  display: 'flex',
  flexDirection: 'column',
  height: '100%',
  width: '100%',
  minWidth: 0,
} as const

/** Apply to MUI Grid items that wrap flex cards for equal-height rows. */
export const flexGridItemSx = {
  display: 'flex',
  minWidth: 0,
} as const

export const flexCardContentSx = {
  display: 'flex',
  flexDirection: 'column',
  flexGrow: 1,
  '&:last-child': { pb: 2 },
} as const

export const flexCardBodySx = {
  display: 'flex',
  flexDirection: 'column',
  flexGrow: 1,
  gap: 1,
  minHeight: 0,
} as const

export const flexCardActionsSx = {
  mt: 'auto',
  pt: 0.5,
} as const

/** Two-line description clamp — display-only truncation; preserves full data in dialogs/forms. */
export const descriptionClampSx = {
  display: '-webkit-box',
  WebkitLineClamp: 2,
  WebkitBoxOrient: 'vertical',
  overflow: 'hidden',
  overflowWrap: 'anywhere',
  wordBreak: 'break-word',
  lineHeight: 1.43,
} as const
