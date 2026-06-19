import { useParams } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'

export function InitiativeLeaderboardPage() {
  const { initiativeId } = useParams()

  return (
    <>
      <PageHeader
        description={
          initiativeId
            ? `Ranking within initiative ${initiativeId}.`
            : 'Ranking within a selected learning initiative.'
        }
        title="Initiative Leaderboard"
      />
      <PlaceholderPanel
        items={[
          initiativeId ? `Initiative ID: ${initiativeId}` : 'Select initiative',
          'Fetch /api/v1/leaderboards/initiatives/{initiativeId}',
          'Show submittedAtUtc-based ranking and tie breakers',
        ]}
        title="Initiative leaderboard structure"
      />
    </>
  )
}
