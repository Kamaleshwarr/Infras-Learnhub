import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface CertificateSubmission {
  id: string
  comments?: string
  submittedAtUtc: string
  approvalStatus: 'SUBMITTED' | 'APPROVED' | 'REJECTED'
  rejectionReason?: string
}

export const submissionsApi = {
  listMine: async () => {
    const response = await httpClient.get<PageResponse<CertificateSubmission>>('/me/submissions')
    return response.data
  },
  submit: async (initiativeId: string, formData: FormData) => {
    const response = await httpClient.post<CertificateSubmission>(
      `/initiatives/${initiativeId}/submissions`,
      formData,
      { headers: { 'Content-Type': 'multipart/form-data' } },
    )
    return response.data
  },
}
