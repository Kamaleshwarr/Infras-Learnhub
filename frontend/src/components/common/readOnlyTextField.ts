import { longTextWrapSx } from './textStyles'

export const readOnlyTextFieldSlotProps = {
  input: {
    readOnly: true,
    sx: longTextWrapSx,
  },
} as const
