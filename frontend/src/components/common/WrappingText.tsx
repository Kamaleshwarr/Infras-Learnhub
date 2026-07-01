import { Typography } from '@mui/material'
import type { TypographyProps } from '@mui/material'
import { longTextWrapSx } from './textStyles'

interface WrappingTextProps extends TypographyProps {
  children: React.ReactNode
}

export function WrappingText({ children, sx, ...props }: WrappingTextProps) {
  return (
    <Typography sx={{ ...longTextWrapSx, ...sx }} {...props}>
      {children}
    </Typography>
  )
}
