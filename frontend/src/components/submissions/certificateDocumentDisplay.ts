export function formatCertificateContentType(contentType: string) {
  switch (contentType.toLowerCase()) {
    case 'application/pdf':
      return 'PDF'
    case 'image/jpeg':
      return 'JPEG'
    case 'image/png':
      return 'PNG'
    default:
      return contentType
  }
}

export function formatFileSizeBytes(bytes: number) {
  if (!Number.isFinite(bytes) || bytes < 0) {
    return '—'
  }

  if (bytes < 1024) {
    return `${bytes} B`
  }

  const units = ['KB', 'MB', 'GB'] as const
  let value = bytes / 1024
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  const precision = Number.isInteger(value) ? 0 : 1
  return `${value.toFixed(precision)} ${units[unitIndex]}`
}

export function isPdfContentType(contentType: string) {
  return contentType.toLowerCase() === 'application/pdf'
}

export function isImageContentType(contentType: string) {
  return contentType.toLowerCase().startsWith('image/')
}
