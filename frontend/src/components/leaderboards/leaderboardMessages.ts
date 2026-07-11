export const LEADERBOARD_MESSAGES = {
  approvedCertification: (count: number) =>
    count === 1 ? '1 approved certification' : `${count} approved certifications`,
  approvedCertificationLabel: 'Approved Certifications',
  currentUserSummary: 'Your standing on the global leaderboard.',
  emptyGlobal: 'No approved certifications yet. Complete and get certifications approved to appear here.',
  emptyInitiative:
    'No approved certifications have been submitted for this initiative yet. Employees will appear here once certifications are approved.',
  errorGlobal: 'Unable to load the global leaderboard. Please refresh or try again later.',
  errorInitiative: 'Unable to load the initiative leaderboard. Please refresh or try again later.',
  globalDescription:
    'Employees ranked by verified approved certifications across all initiatives. All-time results only.',
  globalTitle: 'Global Leaderboard',
  initiativeDescription:
    'Employees ranked by earliest certification submission time within this initiative. Only approved certifications are eligible.',
  initiativeRankingRule:
    'Ranking is based on certification submission time, not approval time. The earliest submitted approved certification ranks highest.',
  initiativeRankedEmployees: 'Ranked employees',
  initiativeYourRank: 'Your rank',
  initiativeSelectLabel: 'Learning initiative',
  initiativeSelectPlaceholder: 'Select an initiative',
  initiativeTab: 'Initiative',
  globalTab: 'Global',
  loadInitiativesError: 'Unable to load initiatives.',
  myRank: 'My Rank',
  myRankUnranked: 'Not ranked yet',
  noInitiativeSelected: 'Select an initiative to view its leaderboard.',
  notFound: 'This initiative leaderboard is not available.',
  rank: 'Rank',
  retry: 'Retry',
  submitted: 'Submitted',
  approved: 'Approved on',
  topPerformers: 'Top Performers',
  viewLeaderboard: 'View Leaderboard',
  viewFullLeaderboard: 'View Full Leaderboard',
} as const
