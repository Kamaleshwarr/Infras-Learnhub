import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function AdminReviewPage() {
  return (
    <>
      <PageHeader
        description="Review pending certificate submissions and approve or reject them."
        title="Certificate Review"
      />
      <PlaceholderPanel
        items={[
          'Fetch /api/v1/submissions?status=SUBMITTED',
          'Display employee, initiative, submitted date, and comments',
          'Approve and reject actions with confirmation dialogs',
        ]}
        title="Admin certificate review structure"
      />
    </>
  )
}
