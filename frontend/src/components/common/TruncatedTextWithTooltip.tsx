import { Tooltip, Typography } from '@mui/material'
import type { TypographyProps } from '@mui/material'
import { truncateText } from './textDisplay'
import { longTextWrapSx, singleLineEllipsisSx } from './textStyles'

interface TruncatedTextWithTooltipProps {
  text: string
  maxLength: number
  variant?: TypographyProps['variant']
  color?: TypographyProps['color']
}

export function TruncatedTextWithTooltip({
  text,
  maxLength,
  variant = 'body2',
  color,
}: TruncatedTextWithTooltipProps) {
  const displayText = truncateText(text, maxLength)
  const isTruncated = displayText !== text

  if (!isTruncated) {
    return (
      <Typography color={color} sx={longTextWrapSx} variant={variant}>
        {displayText}
      </Typography>
    )
  }

  return (
    <Tooltip title={text}>
      <Typography
        color={color}
        component="span"
        sx={{ ...singleLineEllipsisSx, cursor: 'default' }}
        variant={variant}
      >
        {displayText}
      </Typography>
    </Tooltip>
  )
}
