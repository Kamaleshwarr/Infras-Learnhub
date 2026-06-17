import type { PageResponse } from '../types/api'
import type { CertificateSubmission } from '../types/submissions'
import { submissionsApi } from './submissionsApi'

const MAX_SUBMISSION_PAGES = 20

export async function loadAllMySubmissions() {
  const submissions: CertificateSubmission[] = []
  let page = 0
  let last = false

  while (!last && page < MAX_SUBMISSION_PAGES) {
    const response: PageResponse<CertificateSubmission> = await submissionsApi.listMine({
      page,
      size: 100,
      sort: 'submittedAtUtc,desc',
    })

    submissions.push(...(response.content ?? []))
    last = response.last ?? true
    page += 1
  }

  return submissions
}
