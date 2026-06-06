import { useParams } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function InitiativeDetailPage() {
  const { initiativeId } = useParams()

  return (
    <>
      <PageHeader description={`Initiative ID: ${initiativeId ?? 'not selected'}`} title="Initiative Details" />
      <PlaceholderPanel
        items={[
          'Show title, description, reward, status, and UTC date window',
          'Show submission call to action for employees',
          'Link to relevant study materials when backend association is available',
        ]}
        title="Initiative detail structure"
      />
    </>
  )
}
