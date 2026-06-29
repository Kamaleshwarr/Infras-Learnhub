export const longTextWrapSx = {
  overflowWrap: 'anywhere',
  wordBreak: 'break-word',
} as const

export const singleLineEllipsisSx = {
  display: 'block',
  maxWidth: '100%',
  minWidth: 0,
  overflow: 'hidden',
  overflowWrap: 'anywhere',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
} as const
