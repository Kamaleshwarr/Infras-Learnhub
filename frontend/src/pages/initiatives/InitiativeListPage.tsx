import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function InitiativeListPage() {
  return (
    <>
      <PageHeader description="Browse active learning initiatives." title="Learning Initiatives" />
      <PlaceholderPanel
        items={[
          'List active initiatives using /api/v1/initiatives',
          'Support search, pagination, sorting, and status chips',
          'Navigate to initiative details',
        ]}
        title="Initiative list structure"
      />
    </>
  )
}
