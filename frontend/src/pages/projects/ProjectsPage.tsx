import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function ProjectsPage() {
  return (
    <>
      <PageHeader description="Browse accessible project knowledge repositories." title="Projects" />
      <PlaceholderPanel
        items={[
          'Fetch /api/v1/projects',
          'Support public and members-only access indicators',
          'Navigate to project knowledge repository view',
        ]}
        title="Project browser structure"
      />
    </>
  )
}
