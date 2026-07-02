import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { projectsApi } from '../../api/projectsApi'
import { PageHeader } from '../../components/common/PageHeader'
import { PlaceholderPanel } from '../../components/common/PlaceholderPanel'
import { RelatedTechnologiesCard } from '../../components/learn/RelatedTechnologiesCard'
import type { RelatedTechnologySummary } from '../../types/learn'

export function ProjectKnowledgePage() {
  const { projectId } = useParams()
  const [relatedTechnologies, setRelatedTechnologies] = useState<RelatedTechnologySummary[]>([])

  useEffect(() => {
    if (!projectId) {
      return
    }

    let mounted = true

    async function loadProject() {
      try {
        const project = await projectsApi.get(projectId!)
        if (mounted) {
          setRelatedTechnologies(project.relatedTechnologies ?? [])
        }
      } catch {
        if (mounted) {
          setRelatedTechnologies([])
        }
      }
    }

    void loadProject()

    return () => {
      mounted = false
    }
  }, [projectId])

  return (
    <>
      <PageHeader description={`Project ID: ${projectId ?? 'not selected'}`} title="Project Knowledge Repository" />
      <PlaceholderPanel
        items={[
          'Browse project folders',
          'Search requirements, KT, architecture, release notes, test strategy, test data, videos, and links',
          'Download files and access links with tracking',
        ]}
        title="Project knowledge structure"
      />
      <RelatedTechnologiesCard technologies={relatedTechnologies} />
    </>
  )
}
