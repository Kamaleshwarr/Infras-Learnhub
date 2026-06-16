import { describe, expect, it } from 'vitest'
import { CERTIFICATE_MAX_SIZE_BYTES } from './submissionMessages'
import { getCertificateAcceptAttribute, isAllowedCertificateFile } from './certificateFileValidation'

describe('certificateFileValidation', () => {
  it('accepts allowed PDF and image files within the size limit', () => {
    expect(isAllowedCertificateFile(new File(['pdf'], 'certificate.pdf', { type: 'application/pdf' }))).toBe(true)
    expect(isAllowedCertificateFile(new File(['jpg'], 'certificate.jpg', { type: 'image/jpeg' }))).toBe(true)
    expect(isAllowedCertificateFile(new File(['png'], 'certificate.png', { type: 'image/png' }))).toBe(true)
  })

  it('accepts files by extension when the browser omits content type', () => {
    expect(isAllowedCertificateFile(new File(['pdf'], 'certificate.PDF', { type: '' }))).toBe(true)
    expect(isAllowedCertificateFile(new File(['jpg'], 'certificate.JPEG', { type: '' }))).toBe(true)
  })

  it('rejects unsupported types, empty files, and oversized files', () => {
    expect(isAllowedCertificateFile(new File(['doc'], 'certificate.doc', { type: 'application/msword' }))).toBe(false)
    expect(isAllowedCertificateFile(new File([], 'certificate.pdf', { type: 'application/pdf' }))).toBe(false)
    expect(
      isAllowedCertificateFile(
        new File([new Uint8Array(CERTIFICATE_MAX_SIZE_BYTES + 1)], 'certificate.pdf', {
          type: 'application/pdf',
        }),
      ),
    ).toBe(false)
  })

  it('exposes the certificate accept attribute for file inputs', () => {
    expect(getCertificateAcceptAttribute()).toContain('application/pdf')
    expect(getCertificateAcceptAttribute()).toContain('.png')
  })
})
