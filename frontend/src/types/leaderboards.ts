export interface LeaderboardEmployee {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface GlobalLeaderboardEntry {
  rank: number
  employee: LeaderboardEmployee
  totalApprovedCertifications: number
  earliestSubmittedAtUtc: string | null
  latestApprovedAtUtc: string | null
}

export interface InitiativeLeaderboardEntry {
  rank: number
  submissionId: string
  employee: LeaderboardEmployee
  initiativeId: string
  initiativeTitle: string
  submittedAtUtc: string
  approvedAtUtc: string
}

export interface RecentApproval {
  submissionId: string
  initiativeId: string
  initiativeTitle: string
  submittedAtUtc: string
  approvedAtUtc: string
}

export interface PersonalLeaderboard {
  employee: LeaderboardEmployee
  globalRank: number | null
  totalApprovedCertifications: number
  earliestSubmittedAtUtc: string | null
  recentApprovals: RecentApproval[]
}

export const LEADERBOARD_PAGE_SIZE_OPTIONS = [10, 20, 50] as const
export const DEFAULT_LEADERBOARD_PAGE_SIZE = 20
