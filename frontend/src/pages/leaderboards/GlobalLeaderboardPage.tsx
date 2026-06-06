import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function GlobalLeaderboardPage() {
  return (
    <>
      <PageHeader description="Global ranking by approved certifications." title="Global Leaderboard" />
      <PlaceholderPanel
        items={[
          'Fetch /api/v1/leaderboards/global',
          'Display rank, employee, approved count, and earliest submission',
          'Link to initiative leaderboard view',
        ]}
        title="Global leaderboard structure"
      />
    </>
  )
}
