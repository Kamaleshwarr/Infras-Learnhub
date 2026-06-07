import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'

export interface CertificateSubmission {
  id: string
  comments?: string
  submittedAtUtc: string
  approvalStatus: 'SUBMITTED' | 'APPROVED' | 'REJECTED'
  rejectionReason?: string
}

export interface SubmissionListParams {
  page?: number
  size?: number
  sort?: string
  status?: CertificateSubmission['approvalStatus']
  initiativeId?: string
  employeeId?: string
}

export const submissionsApi = {
  listMine: async (params?: Omit<SubmissionListParams, 'employeeId'>) => {
    const response = await httpClient.get<PageResponse<CertificateSubmission>>('/me/submissions', { params })
    return response.data
  },
  listAll: async (params?: SubmissionListParams) => {
    const response = await httpClient.get<PageResponse<CertificateSubmission>>('/submissions', { params })
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
