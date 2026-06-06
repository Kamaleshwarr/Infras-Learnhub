import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function InitiativeLeaderboardPage() {
  return (
    <>
      <PageHeader description="Ranking within a selected learning initiative." title="Initiative Leaderboard" />
      <PlaceholderPanel
        items={[
          'Select initiative',
          'Fetch /api/v1/leaderboards/initiatives/{initiativeId}',
          'Show submittedAtUtc-based ranking and tie breakers',
        ]}
        title="Initiative leaderboard structure"
      />
    </>
  )
}
