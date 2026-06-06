import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function MySubmissionsPage() {
  return (
    <>
      <PageHeader description="Track submitted, approved, and rejected certificates." title="My Submissions" />
      <PlaceholderPanel
        items={[
          'Fetch /api/v1/me/submissions',
          'Display approval status and rejection reason',
          'Support pagination and sorting',
        ]}
        title="My submissions structure"
      />
    </>
  )
}
