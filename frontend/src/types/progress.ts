export type LearningEnrollmentStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'LEFT'

export interface Enrollment {
  id: string
  technologyId: string
  technologySlug: string
  technologyName: string
  status: LearningEnrollmentStatus
  enrolledAt: string
  startedAt?: string | null
  lastActivityAt?: string | null
  completedAt?: string | null
  progressPercent: number
  currentStageId?: string | null
  currentStageOrder?: number | null
  currentStageTitle?: string | null
  nextStageId?: string | null
  nextStageOrder?: number | null
  nextStageTitle?: string | null
}

export interface ContinueLearning {
  enrollmentId: string
  technologyId: string
  technologySlug: string
  technologyName: string
  currentStageId: string
  currentStageOrder: number
  currentStageTitle: string
  progressPercent: number
}

export interface Journey {
  continueLearning?: ContinueLearning | null
  active: Enrollment[]
  completed: Enrollment[]
  left: Enrollment[]
}

export interface TechnologyProgress {
  enrollmentId: string
  technologyId: string
  technologySlug: string
  technologyName: string
  status: LearningEnrollmentStatus
  enrolledAt: string
  startedAt?: string | null
  lastActivityAt?: string | null
  completedAt?: string | null
  progressPercent: number
  totalStages: number
  completedStageCount: number
  currentStageId?: string | null
  currentStageOrder?: number | null
  currentStageTitle?: string | null
  nextStageId?: string | null
  nextStageOrder?: number | null
  nextStageTitle?: string | null
  estimatedRemainingEffort: string
  completedStageIds: string[]
  completedStageOrders: number[]
}
