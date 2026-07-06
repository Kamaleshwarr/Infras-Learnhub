import type { RelatedTechnologySummary } from './learn'

export type ProjectStatus = 'ACTIVE' | 'ON_HOLD' | 'COMPLETED' | 'ARCHIVED'
export type ProjectAccessType = 'PUBLIC' | 'MEMBERS_ONLY'
export type ProjectRole = 'OWNER' | 'CONTRIBUTOR' | 'VIEWER'

export interface ProjectUserSummary {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface ProjectSummary {
  id: string
  name: string
  description?: string
  accessType: ProjectAccessType
  status: ProjectStatus
  archived: boolean
  owner?: ProjectUserSummary | null
  memberCount?: number | null
  currentMemberRole?: ProjectRole | null
  relatedTechnologies?: RelatedTechnologySummary[]
  createdAtUtc?: string
  updatedAtUtc?: string
}

export interface ProjectDetail extends ProjectSummary {
  createdBy: ProjectUserSummary
}

export interface ProjectMember {
  id: string
  projectId: string
  user: ProjectUserSummary
  projectRole: ProjectRole
  createdAtUtc: string
  updatedAtUtc: string
}

export interface CreateProjectPayload {
  name: string
  description?: string
  accessType: ProjectAccessType
}

export interface UpdateProjectPayload {
  name: string
  description?: string
  accessType: ProjectAccessType
  status: ProjectStatus
}

export interface ProjectMemberPayload {
  userId: string
  projectRole: ProjectRole
}

export const PROJECT_STATUS_LABELS: Record<ProjectStatus, string> = {
  ACTIVE: 'Active',
  ON_HOLD: 'On Hold',
  COMPLETED: 'Completed',
  ARCHIVED: 'Archived',
}

export const PROJECT_ACCESS_LABELS: Record<ProjectAccessType, string> = {
  PUBLIC: 'Public',
  MEMBERS_ONLY: 'Members Only',
}

export const PROJECT_ROLE_LABELS: Record<ProjectRole, string> = {
  OWNER: 'Owner',
  CONTRIBUTOR: 'Contributor',
  VIEWER: 'Viewer',
}

export const DEFAULT_PROJECT_LIST_QUERY = {
  page: 0,
  size: 12,
  search: '',
  status: '' as ProjectStatus | '',
  accessType: '' as ProjectAccessType | '',
  assigned: false,
  includeArchived: false,
  sort: 'name,asc',
}

export type ProjectListQuery = typeof DEFAULT_PROJECT_LIST_QUERY
