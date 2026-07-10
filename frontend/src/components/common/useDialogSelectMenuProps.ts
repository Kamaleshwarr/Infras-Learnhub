import { useCallback, useRef } from 'react'
import type { MouseEvent } from 'react'
import type { SelectProps } from '@mui/material/Select'

/**
 * MUI Select opens on mousedown. The opening click's mouseup can land on a MenuItem
 * after the menu renders under the pointer. Built-in guards only delay unselected
 * item mouseup for 200ms, which is shorter than a typical click hold.
 *
 * Consume only the opening gesture's first menu mouseup (capture phase). A subsequent
 * mousedown on a menu item clears the guard so explicit clicks still work.
 */
export function useDialogSelectMenuProps() {
  const openingGestureActiveRef = useRef(false)

  const onOpen = useCallback(() => {
    openingGestureActiveRef.current = true
  }, [])

  const onClose = useCallback(() => {
    openingGestureActiveRef.current = false
  }, [])

  const menuProps: NonNullable<SelectProps['MenuProps']> = {
    disableAutoFocusItem: true,
    slotProps: {
      paper: {
        onMouseDown: (event: MouseEvent<HTMLDivElement>) => {
          event.preventDefault()
        },
      },
      list: {
        onMouseDownCapture: () => {
          openingGestureActiveRef.current = false
        },
        onMouseUpCapture: (event: MouseEvent<HTMLUListElement>) => {
          if (!openingGestureActiveRef.current) {
            return
          }
          event.preventDefault()
          event.stopPropagation()
          openingGestureActiveRef.current = false
        },
      },
    },
  }

  return { menuProps, onOpen, onClose }
}
