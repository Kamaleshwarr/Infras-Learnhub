export type ProjectFunctionalRole =
  | 'PRODUCT_OWNER'
  | 'PROJECT_MANAGER'
  | 'BUSINESS_ANALYST'
  | 'TECH_LEAD'
  | 'DEVELOPER'
  | 'QA_ENGINEER'
  | 'QA_LEAD'
  | 'AUTOMATION_ENGINEER'
  | 'DEVOPS_ENGINEER'
  | 'UI_UX_DESIGNER'
  | 'ARCHITECT'
  | 'SCRUM_MASTER'
  | 'SUPPORT'
  | 'OTHER'

export type ExternalContactType =
  | 'CLIENT'
  | 'VENDOR'
  | 'BUSINESS'
  | 'INFRASTRUCTURE'
  | 'SECURITY'
  | 'SUPPORT'
  | 'OTHER'

export interface ProjectMemberCandidate {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface ProjectExternalContact {
  id: string
  projectId: string
  name: string
  contactType: ExternalContactType
  roleTitle?: string | null
  organization?: string | null
  email?: string | null
  phone?: string | null
  contactUrl?: string | null
  notes?: string | null
  primaryContact: boolean
  displayOrder: number
  active: boolean
  createdBy: {
    id: string
    employeeId: string
    fullName: string
    email: string
  }
  createdAtUtc: string
  updatedAtUtc: string
}

export interface ProjectExternalContactPayload {
  name: string
  contactType: ExternalContactType
  roleTitle?: string
  organization?: string
  email?: string
  phone?: string
  contactUrl?: string
  notes?: string
  primaryContact?: boolean
  displayOrder?: number
  active?: boolean
}

export const PROJECT_FUNCTIONAL_ROLE_LABELS: Record<ProjectFunctionalRole, string> = {
  PRODUCT_OWNER: 'Product Owner',
  PROJECT_MANAGER: 'Project Manager',
  BUSINESS_ANALYST: 'Business Analyst',
  TECH_LEAD: 'Tech Lead',
  DEVELOPER: 'Developer',
  QA_ENGINEER: 'QA Engineer',
  QA_LEAD: 'QA Lead',
  AUTOMATION_ENGINEER: 'Automation Engineer',
  DEVOPS_ENGINEER: 'DevOps Engineer',
  UI_UX_DESIGNER: 'UI/UX Designer',
  ARCHITECT: 'Architect',
  SCRUM_MASTER: 'Scrum Master',
  SUPPORT: 'Support',
  OTHER: 'Other',
}

export const EXTERNAL_CONTACT_TYPE_LABELS: Record<ExternalContactType, string> = {
  CLIENT: 'Client',
  VENDOR: 'Vendor',
  BUSINESS: 'Business',
  INFRASTRUCTURE: 'Infrastructure',
  SECURITY: 'Security',
  SUPPORT: 'Support',
  OTHER: 'Other',
}
