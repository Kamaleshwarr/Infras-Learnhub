import { describe, expect, it } from 'vitest'
import {
  formatCertificateContentType,
  formatFileSizeBytes,
  isImageContentType,
  isPdfContentType,
} from './certificateDocumentDisplay'

describe('certificateDocumentDisplay', () => {
  it('formats certificate content types', () => {
    expect(formatCertificateContentType('application/pdf')).toBe('PDF')
    expect(formatCertificateContentType('image/png')).toBe('PNG')
    expect(formatCertificateContentType('image/jpeg')).toBe('JPEG')
  })

  it('formats file sizes', () => {
    expect(formatFileSizeBytes(512)).toBe('512 B')
    expect(formatFileSizeBytes(1024)).toBe('1 KB')
    expect(formatFileSizeBytes(1536)).toBe('1.5 KB')
  })

  it('detects previewable content types', () => {
    expect(isPdfContentType('application/pdf')).toBe(true)
    expect(isImageContentType('image/png')).toBe(true)
    expect(isPdfContentType('text/plain')).toBe(false)
  })
})
