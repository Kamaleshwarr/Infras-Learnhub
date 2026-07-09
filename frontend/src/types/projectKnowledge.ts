export type KnowledgeCategory =
  | 'REQUIREMENTS'
  | 'KT_DOCUMENTS'
  | 'ARCHITECTURE_DOCUMENTS'
  | 'RELEASE_NOTES'
  | 'TEST_STRATEGY'
  | 'TEST_DATA_DOCUMENTATION'
  | 'KT_VIDEOS'
  | 'EXTERNAL_LINKS'

export type KnowledgeSourceType = 'FILE' | 'LINK'

export interface ProjectKnowledgeUser {
  id: string
  employeeId: string
  fullName: string
  email: string
}

export interface ProjectKnowledgeFolder {
  id: string
  projectId: string
  name: string
  description?: string
  parentId?: string | null
  createdBy: ProjectKnowledgeUser
  createdAtUtc: string
  updatedAtUtc: string
  childFolderCount: number
  itemCount: number
}

export interface ProjectKnowledgeItem {
  id: string
  projectId: string
  folderId?: string | null
  folderName?: string | null
  title: string
  description?: string
  category: KnowledgeCategory
  sourceType: KnowledgeSourceType
  originalFilename?: string | null
  contentType?: string | null
  fileSizeBytes?: number | null
  externalUrl?: string | null
  accessCount: number
  uploadedBy: ProjectKnowledgeUser
  createdAtUtc: string
  updatedAtUtc: string
}

export interface ProjectFolderPayload {
  name: string
  description?: string
  parentId?: string | null
}

export interface ProjectKnowledgeLinkPayload {
  folderId?: string | null
  title: string
  description?: string
  category: KnowledgeCategory
  externalUrl: string
}

export interface ProjectKnowledgeItemUpdatePayload {
  folderId?: string | null
  title: string
  description?: string
  category: KnowledgeCategory
  externalUrl?: string
}

export interface ProjectLinkAccessResponse {
  itemId: string
  externalUrl: string
  accessCount: number
}

export const KNOWLEDGE_CATEGORY_LABELS: Record<KnowledgeCategory, string> = {
  REQUIREMENTS: 'Requirements',
  KT_DOCUMENTS: 'Technical Documentation',
  ARCHITECTURE_DOCUMENTS: 'Architecture',
  RELEASE_NOTES: 'Release Notes',
  TEST_STRATEGY: 'Test Strategy',
  TEST_DATA_DOCUMENTATION: 'Test Data',
  KT_VIDEOS: 'Videos',
  EXTERNAL_LINKS: 'Useful Link',
}

/** Maximum folder levels under Knowledge Base root (Level 1–3). */
export const KNOWLEDGE_FOLDER_MAX_DEPTH = 3
