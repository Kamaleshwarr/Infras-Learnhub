export type RoadmapResourceType =
  | 'OFFICIAL_DOCUMENTATION'
  | 'OFFICIAL_TUTORIAL'
  | 'OPEN_EDUCATIONAL_RESOURCE'
  | 'VIDEO'
  | 'ARTICLE'
  | 'GITHUB'
  | 'INTERACTIVE_TUTORIAL'
  | 'PRACTICE_LAB'
  | 'OTHER'

export type RoadmapResourceCost = 'FREE' | 'PAID' | 'FREEMIUM'

export interface RoadmapResource {
  slug: string
  title: string
  url: string
  type: RoadmapResourceType
  provider: string
  freePaid?: RoadmapResourceCost | null
}

export interface RoadmapStage {
  id: string
  order: number
  slug: string
  title: string
  description: string
  estimatedEffort: string
  notes?: string | null
  learningResources: RoadmapResource[]
  practiceResources: RoadmapResource[]
}

export interface Roadmap {
  technologyId: string
  technologySlug: string
  technologyName: string
  version: string
  description?: string | null
  source?: string | null
  sourceUrl?: string | null
  catalogUpdatedAt?: string | null
  stageCount: number
  estimatedTotalEffort: string
  recommendedStageOrder: number
  nextStageOrder: number
  stages: RoadmapStage[]
}
