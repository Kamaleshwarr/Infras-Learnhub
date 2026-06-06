import { useParams } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function ProjectKnowledgePage() {
  const { projectId } = useParams()

  return (
    <>
      <PageHeader description={`Project ID: ${projectId ?? 'not selected'}`} title="Project Knowledge Repository" />
      <PlaceholderPanel
        items={[
          'Browse project folders',
          'Search requirements, KT, architecture, release notes, test strategy, test data, videos, and links',
          'Download files and access links with tracking',
        ]}
        title="Project knowledge structure"
      />
    </>
  )
}
