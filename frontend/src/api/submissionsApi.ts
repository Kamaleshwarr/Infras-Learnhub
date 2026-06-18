import { httpClient } from './httpClient'
import type { PageResponse } from '../types/api'
import type {
  CertificateSubmission,
  RejectSubmissionRequest,
  SubmissionListParams,
} from '../types/submissions'

export const submissionsApi = {
  listMine: async (params?: Omit<SubmissionListParams, 'employeeId'>) => {
    const response = await httpClient.get<PageResponse<CertificateSubmission>>('/me/submissions', { params })
    return response.data
  },
  listAll: async (params?: SubmissionListParams) => {
    const response = await httpClient.get<PageResponse<CertificateSubmission>>('/submissions', { params })
    return response.data
  },
  getById: async (submissionId: string) => {
    const response = await httpClient.get<CertificateSubmission>(`/submissions/${submissionId}`)
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
  approve: async (submissionId: string) => {
    const response = await httpClient.post<CertificateSubmission>(`/submissions/${submissionId}/approve`)
    return response.data
  },
  reject: async (submissionId: string, body: RejectSubmissionRequest) => {
    const response = await httpClient.post<CertificateSubmission>(`/submissions/${submissionId}/reject`, body)
    return response.data
  },
  getCertificateBlob: async (
    submissionId: string,
    options?: { disposition?: 'inline' | 'attachment' },
  ) => {
    const response = await httpClient.get<Blob>(`/submissions/${submissionId}/certificate`, {
      params: { disposition: options?.disposition ?? 'attachment' },
      responseType: 'blob',
    })
    return {
      blob: response.data,
      contentType: response.headers['content-type'] ?? 'application/octet-stream',
    }
  },
}
