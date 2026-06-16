export const CERTIFICATE_ACCEPT = 'application/pdf,image/jpeg,image/png'

export const CERTIFICATE_MAX_SIZE_BYTES = 26_214_400

export const MAX_SUBMISSION_COMMENTS_LENGTH = 2000

export const MAX_REJECTION_REASON_LENGTH = 2000

export const SUBMISSION_MESSAGES = {
  submitSuccess: 'Certificate submitted successfully.',
  approveSuccess: 'Certificate approved.',
  rejectSuccess: 'Certificate rejected.',
  loadError: 'Unable to load certificate submissions. Please try again.',
  submitError: 'Unable to submit certificate. Please try again.',
  approveError: 'Unable to approve certificate submission. Please try again.',
  rejectError: 'Unable to reject certificate submission. Please try again.',
  initiativeRequired: 'Select an initiative.',
  fileRequired: 'Certificate file is required.',
  fileValidationError: 'Certificate file must be a PDF, JPEG, or PNG and within the size limit.',
  rejectionReasonRequired: 'Rejection reason is required.',
  duplicateSubmission: 'A certificate submission already exists for this initiative.',
} as const
