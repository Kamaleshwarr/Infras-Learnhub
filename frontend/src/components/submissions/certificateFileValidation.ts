import { CERTIFICATE_ACCEPT, CERTIFICATE_MAX_SIZE_BYTES } from './submissionMessages'

const ALLOWED_CERTIFICATE_TYPES = new Set(['application/pdf', 'image/jpeg', 'image/png'])

const ALLOWED_CERTIFICATE_EXTENSIONS = new Set(['.pdf', '.jpg', '.jpeg', '.png'])

export function isAllowedCertificateFile(file: File) {
  if (file.size <= 0 || file.size > CERTIFICATE_MAX_SIZE_BYTES) {
    return false
  }

  const normalizedType = file.type.toLowerCase()
  if (ALLOWED_CERTIFICATE_TYPES.has(normalizedType)) {
    return true
  }

  const extensionIndex = file.name.lastIndexOf('.')
  if (extensionIndex === -1) {
    return false
  }

  const extension = file.name.slice(extensionIndex).toLowerCase()
  return ALLOWED_CERTIFICATE_EXTENSIONS.has(extension)
}

export function getCertificateAcceptAttribute() {
  return `${CERTIFICATE_ACCEPT},.pdf,.jpg,.jpeg,.png`
}
