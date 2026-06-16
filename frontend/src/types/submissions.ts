export type ApprovalStatus = 'SUBMITTED' | 'APPROVED' | 'REJECTED'

export type InitiativeStatus = 'DRAFT' | 'ACTIVE' | 'EXPIRED'

export interface SubmissionEmployee {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface SubmissionInitiative {
  id: string
  title: string
  status: InitiativeStatus
}

export interface CertificateDocument {
  id: string
  originalFilename: string
  contentType: string
  fileSizeBytes: number
}

export interface CertificateSubmission {
  id: string
  employee: SubmissionEmployee
  initiative: SubmissionInitiative
  certificateDocumentId: string
  certificateDocument: CertificateDocument
  comments?: string | null
  submittedAtUtc: string
  approvalStatus: ApprovalStatus
  reviewedBy?: SubmissionEmployee | null
  reviewedAtUtc?: string | null
  rejectionReason?: string | null
  createdAtUtc: string
  updatedAtUtc: string
}

export interface RejectSubmissionRequest {
  rejectionReason: string
}

export interface SubmissionListParams {
  page?: number
  size?: number
  sort?: string
  status?: ApprovalStatus
  initiativeId?: string
  employeeId?: string
}
