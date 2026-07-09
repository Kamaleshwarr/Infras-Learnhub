export type EnvironmentReferenceType =
  | 'APPLICATION'
  | 'API_BASE'
  | 'SWAGGER'
  | 'ADMIN_PORTAL'
  | 'EMPLOYEE_PORTAL'
  | 'AGENT_PORTAL'
  | 'MONITORING'
  | 'LOGS'
  | 'CICD_PIPELINE'
  | 'DEPLOYMENT'
  | 'API_GATEWAY'
  | 'DATABASE_ADMIN'
  | 'OTHER'

export type RepositoryProvider = 'GITHUB' | 'GITLAB' | 'BITBUCKET' | 'AZURE_DEVOPS' | 'OTHER'

export type RepositoryType =
  | 'BACKEND'
  | 'FRONTEND'
  | 'FULL_STACK'
  | 'MOBILE'
  | 'AUTOMATION'
  | 'INFRASTRUCTURE'
  | 'DATABASE'
  | 'PERFORMANCE_TESTING'
  | 'SHARED_LIBRARY'
  | 'DOCUMENTATION'
  | 'OTHER'

export interface ProjectEnvironmentReference {
  id: string
  environmentId: string
  name: string
  referenceType: EnvironmentReferenceType
  url: string
  description?: string
  displayOrder: number
  active: boolean
  createdAtUtc: string
  updatedAtUtc: string
}

export interface ProjectEnvironment {
  id: string
  projectId: string
  name: string
  description?: string
  displayOrder: number
  active: boolean
  createdBy: { id: string; employeeId: string; fullName: string; email: string }
  createdAtUtc: string
  updatedAtUtc: string
  referenceCount: number
  references: ProjectEnvironmentReference[]
}

export interface ProjectLinkedRepository {
  id: string
  projectId: string
  name: string
  description?: string
  repositoryType: RepositoryType
  provider: RepositoryProvider
  repositoryUrl: string
  defaultBranch?: string | null
  displayOrder: number
  active: boolean
  createdBy: { id: string; employeeId: string; fullName: string; email: string }
  createdAtUtc: string
  updatedAtUtc: string
}

export interface ProjectEnvironmentPayload {
  name: string
  description?: string
  displayOrder?: number
  active?: boolean
}

export interface ProjectEnvironmentReferencePayload {
  name: string
  referenceType: EnvironmentReferenceType
  url: string
  description?: string
  displayOrder?: number
  active?: boolean
}

export interface ProjectLinkedRepositoryPayload {
  name: string
  description?: string
  repositoryType: RepositoryType
  provider: RepositoryProvider
  repositoryUrl: string
  defaultBranch?: string
  displayOrder?: number
  active?: boolean
}

export const ENVIRONMENT_REFERENCE_TYPE_LABELS: Record<EnvironmentReferenceType, string> = {
  APPLICATION: 'Application',
  API_BASE: 'API Base URL',
  SWAGGER: 'Swagger / OpenAPI',
  ADMIN_PORTAL: 'Admin Portal',
  EMPLOYEE_PORTAL: 'Employee Portal',
  AGENT_PORTAL: 'Agent Portal',
  MONITORING: 'Monitoring',
  LOGS: 'Logs',
  CICD_PIPELINE: 'CI/CD Pipeline',
  DEPLOYMENT: 'Deployment',
  API_GATEWAY: 'API Gateway',
  DATABASE_ADMIN: 'Database Admin',
  OTHER: 'Other',
}

export const REPOSITORY_TYPE_LABELS: Record<RepositoryType, string> = {
  BACKEND: 'Backend',
  FRONTEND: 'Frontend',
  FULL_STACK: 'Full Stack',
  MOBILE: 'Mobile',
  AUTOMATION: 'Automation',
  INFRASTRUCTURE: 'Infrastructure',
  DATABASE: 'Database',
  PERFORMANCE_TESTING: 'Performance Testing',
  SHARED_LIBRARY: 'Shared Library',
  DOCUMENTATION: 'Documentation',
  OTHER: 'Other',
}

export const REPOSITORY_PROVIDER_LABELS: Record<RepositoryProvider, string> = {
  GITHUB: 'GitHub',
  GITLAB: 'GitLab',
  BITBUCKET: 'Bitbucket',
  AZURE_DEVOPS: 'Azure DevOps',
  OTHER: 'Other',
}
