import { useCallback, useRef } from 'react'
import type { MouseEvent, SyntheticEvent } from 'react'
import type { SelectProps } from '@mui/material/Select'

type AnchorPosition = { top: number; left: number }

const baseDialogSelectMenuProps: NonNullable<SelectProps['MenuProps']> = {
  disableAutoFocusItem: true,
  anchorOrigin: { vertical: 'top', horizontal: 'left' },
  transformOrigin: { vertical: 'bottom', horizontal: 'left' },
  slotProps: {
    paper: {
      onMouseDown: (event: MouseEvent<HTMLDivElement>) => {
        event.preventDefault()
      },
    },
    list: {
      onMouseDown: (event: MouseEvent<HTMLUListElement>) => {
        event.preventDefault()
      },
    },
  },
}

export function useDialogSelectMenuProps() {
  const anchorPositionRef = useRef<AnchorPosition>({ top: 0, left: 0 })

  const onOpen = useCallback((event: SyntheticEvent) => {
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect()
    anchorPositionRef.current = { top: rect.top, left: rect.left }
  }, [])

  const menuProps: NonNullable<SelectProps['MenuProps']> = {
    ...baseDialogSelectMenuProps,
    anchorReference: 'anchorPosition',
    anchorPosition: anchorPositionRef.current,
  }

  return { menuProps, onOpen }
}
