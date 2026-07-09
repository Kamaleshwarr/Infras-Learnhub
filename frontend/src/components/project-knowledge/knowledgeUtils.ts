import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'
import { KNOWLEDGE_FOLDER_MAX_DEPTH } from '../../types/projectKnowledge'
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

/** Folder depth under Knowledge Base root: 0 at home, 1–3 inside folders. */
export function getFolderDepthFromBreadcrumbs(breadcrumbItems: KnowledgeBreadcrumbItem[]) {
  return Math.max(0, breadcrumbItems.length - 2)
}

export function canCreateSubfolderAtDepth(folderDepth: number) {
  return folderDepth < KNOWLEDGE_FOLDER_MAX_DEPTH
}

export async function loadAllFoldersForSelect(projectId: string) {
  const collected: ProjectKnowledgeFolder[] = []
  const root = await projectKnowledgeApi.listFolders(projectId, { size: 100, sort: 'name,asc' })
  collected.push(...root.content)

  for (const level1 of root.content) {
    const level2Response = await projectKnowledgeApi.listFolders(projectId, {
      parentId: level1.id,
      size: 100,
      sort: 'name,asc',
    })
    collected.push(...level2Response.content)

    for (const level2 of level2Response.content) {
      const level3Response = await projectKnowledgeApi.listFolders(projectId, {
        parentId: level2.id,
        size: 100,
        sort: 'name,asc',
      })
      collected.push(...level3Response.content)
    }
  }

  return collected
}
