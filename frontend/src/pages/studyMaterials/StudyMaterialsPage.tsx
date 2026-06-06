import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function StudyMaterialsPage() {
  return (
    <>
      <PageHeader description="Browse folders, search resources, and download study materials." title="Study Materials" />
      <PlaceholderPanel
        items={[
          'Folder navigation using /study-materials/folders',
          'Search using /study-materials/materials',
          'Download files and access links with tracking',
        ]}
        title="Study material repository structure"
      />
    </>
  )
}
