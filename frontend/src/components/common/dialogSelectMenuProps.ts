import type { MouseEvent } from 'react'
import type { SelectProps } from '@mui/material/Select'

/**
 * MUI Select inside a Dialog: the mousedown that opens the menu can leave the
 * pointer over a MenuItem when mouseup fires, which selects that item immediately.
 * Prevent default on the menu paper mousedown so only an explicit option click selects.
 */
export const dialogSelectMenuProps: NonNullable<SelectProps['MenuProps']> = {
  disableAutoFocusItem: true,
  slotProps: {
    paper: {
      onMouseDown: (event: MouseEvent<HTMLDivElement>) => {
        event.preventDefault()
      },
    },
  },
}
