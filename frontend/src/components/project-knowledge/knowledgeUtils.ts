import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'
import { KNOWLEDGE_FOLDER_UI_MAX_PARENT_DEPTH } from '../../types/projectKnowledge'
import { projectKnowledgeApi } from '../../api/projectKnowledgeApi'
import type { KnowledgeBreadcrumbItem } from './KnowledgeBreadcrumbs'

export async function buildFolderBreadcrumbs(
  projectId: string,
  projectName: string,
  folderId?: string,
): Promise<KnowledgeBreadcrumbItem[]> {
  const items: KnowledgeBreadcrumbItem[] = [
    { label: projectName, href: `/projects/${projectId}` },
    { label: 'Knowledge Base', href: `/projects/${projectId}/knowledge` },
  ]

  if (!folderId) {
    return items
  }

  const chain: ProjectKnowledgeFolder[] = []
  let currentId: string | null = folderId
  while (currentId) {
    const folder = await projectKnowledgeApi.getFolder(projectId, currentId)
    chain.unshift(folder)
    currentId = folder.parentId ?? null
  }

  for (const folder of chain) {
    items.push({
      label: folder.name,
      href: `/projects/${projectId}/knowledge/folders/${folder.id}`,
    })
  }

  return items
}

export function getFolderDepth(folder: ProjectKnowledgeFolder | null | undefined) {
  if (!folder?.parentId) {
    return 0
  }
  return 1
}

export function canCreateSubfolder(currentFolder: ProjectKnowledgeFolder | null | undefined) {
  if (!currentFolder) {
    return true
  }
  return getFolderDepth(currentFolder) < KNOWLEDGE_FOLDER_UI_MAX_PARENT_DEPTH
}

export async function loadAllFoldersForSelect(projectId: string) {
  const collected: ProjectKnowledgeFolder[] = []
  const root = await projectKnowledgeApi.listFolders(projectId, { size: 100, sort: 'name,asc' })
  collected.push(...root.content)

  for (const area of root.content) {
    const children = await projectKnowledgeApi.listFolders(projectId, {
      parentId: area.id,
      size: 100,
      sort: 'name,asc',
    })
    collected.push(...children.content)
  }

  return collected
}
