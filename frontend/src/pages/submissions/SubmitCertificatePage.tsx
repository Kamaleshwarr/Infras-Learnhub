import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function SubmitCertificatePage() {
  return (
    <>
      <PageHeader description="Upload a certificate file and comments for an active initiative." title="Submit Certificate" />
      <PlaceholderPanel
        items={[
          'Select initiative',
          'Upload PDF/JPEG/PNG certificate',
          'Add comments',
          'Submit multipart form to /initiatives/{initiativeId}/submissions',
        ]}
        title="Certificate submission structure"
      />
    </>
  )
}
