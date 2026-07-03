import type { RoadmapResource, RoadmapResourceCost, RoadmapResourceType } from './roadmap'

export type ResourceOverrideStatus =
  | 'DEFAULT'
  | 'URL_OVERRIDE'
  | 'DISABLED'
  | 'PREFERRED'
  | 'ORGANIZATION'
  | 'INACTIVE'

export type RoadmapResourceKind = 'LEARNING' | 'PRACTICE'

export interface ResourceOverride {
  id: string
  technologySlug: string
  stageSlug: string
  resourceSlug: string
  catalogResourceSlug?: string | null
  resourceKind: RoadmapResourceKind
  disabled: boolean
  overrideUrl?: string | null
  preferred: boolean
  enabled: boolean
  reason?: string | null
  title?: string | null
  resourceType?: RoadmapResourceType | null
  provider?: string | null
  freePaid?: RoadmapResourceCost | null
  resourceOrder: number
  organizationResource: boolean
  status: ResourceOverrideStatus
}

export interface ManagedResource {
  catalog: RoadmapResource | null
  effective: RoadmapResource | null
  override: ResourceOverride | null
  overrideStatus: ResourceOverrideStatus
}

export interface StageResourceAdminView {
  stageSlug: string
  stageTitle: string
  stageOrder: number
  resources: ManagedResource[]
}

export interface CreateResourceOverrideRequest {
  stageSlug: string
  catalogResourceSlug?: string | null
  resourceSlug: string
  resourceKind: RoadmapResourceKind
  disabled?: boolean
  overrideUrl?: string | null
  preferred?: boolean
  enabled?: boolean
  reason?: string | null
  title?: string | null
  resourceType?: RoadmapResourceType | null
  provider?: string | null
  freePaid?: RoadmapResourceCost | null
  resourceOrder?: number | null
}

export interface UpdateResourceOverrideRequest {
  disabled?: boolean
  overrideUrl?: string | null
  preferred?: boolean
  enabled?: boolean
  reason?: string | null
  title?: string | null
  resourceType?: RoadmapResourceType | null
  provider?: string | null
  freePaid?: RoadmapResourceCost | null
  resourceOrder?: number | null
}
