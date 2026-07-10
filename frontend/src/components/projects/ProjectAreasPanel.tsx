import type { ReactNode } from 'react'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import HubOutlinedIcon from '@mui/icons-material/HubOutlined'
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import { Card, CardActionArea, CardContent, Chip, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { PROJECT_MESSAGES } from './projectMessages'

interface ProjectAreaItem {
  icon: ReactNode
  title: string
  description: string
  href?: string
  comingSoon?: boolean
}

interface ProjectAreasPanelProps {
  projectId: string
  environmentCount?: number | null
  repositoryCount?: number | null
  memberCount?: number | null
  primaryContactCount?: number | null
}

export function ProjectAreasPanel({
  environmentCount,
  memberCount,
  primaryContactCount,
  projectId,
  repositoryCount,
}: ProjectAreasPanelProps) {
  const areas: ProjectAreaItem[] = [
    {
      icon: <FolderOpenOutlinedIcon color="primary" />,
      title: PROJECT_MESSAGES.knowledgeBase,
      description: 'Organized folders for requirements, documentation, and operational links.',
      href: `/projects/${projectId}/knowledge`,
    },
    {
      icon: <HubOutlinedIcon color="primary" />,
      title: PROJECT_MESSAGES.environments,
      description:
        environmentCount != null && environmentCount > 0
          ? `${environmentCount} configured environment${environmentCount === 1 ? '' : 's'}`
          : 'Environment URLs for QA, UAT, production, and custom targets.',
      href: `/projects/${projectId}/environments`,
    },
    {
      icon: <StorageOutlinedIcon color="primary" />,
      title: PROJECT_MESSAGES.repositories,
      description:
        repositoryCount != null && repositoryCount > 0
          ? `${repositoryCount} linked repositor${repositoryCount === 1 ? 'y' : 'ies'}`
          : 'Git repositories and source code entry points.',
      href: `/projects/${projectId}/repositories`,
    },
    {
      icon: <GroupsOutlinedIcon color="primary" />,
      title: PROJECT_MESSAGES.teamContacts,
      description:
        memberCount != null && memberCount > 0
          ? `${memberCount} team member${memberCount === 1 ? '' : 's'}${primaryContactCount ? ` · ${primaryContactCount} primary contact${primaryContactCount === 1 ? '' : 's'}` : ''}`
          : 'Team directory, responsibilities, and operational contacts.',
      href: `/projects/${projectId}/team`,
    },
  ]

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <div>
            <Typography variant="h6">{PROJECT_MESSAGES.projectAreasTitle}</Typography>
            <Typography color="text.secondary" variant="body2">
              {PROJECT_MESSAGES.projectAreasDescription}
            </Typography>
          </div>
          <Stack spacing={1.5}>
            {areas.map((area) => (
              <Card key={area.title} sx={{ bgcolor: area.comingSoon ? 'action.hover' : 'background.paper' }} variant="outlined">
                <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
                  {area.href && !area.comingSoon ? (
                    <CardActionArea component={RouterLink} to={area.href}>
                      <AreaContent area={area} />
                    </CardActionArea>
                  ) : (
                    <BoxPadding>
                      <AreaContent area={area} />
                    </BoxPadding>
                  )}
                </CardContent>
              </Card>
            ))}
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  )
}

function BoxPadding({ children }: { children: ReactNode }) {
  return <div style={{ padding: 16 }}>{children}</div>
}

function AreaContent({ area }: { area: ProjectAreaItem }) {
  return (
    <Stack direction="row" spacing={2} sx={{ alignItems: 'flex-start', p: 2 }}>
      {area.icon}
      <Stack spacing={0.5} sx={{ flex: 1 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <Typography variant="subtitle1">{area.title}</Typography>
          {area.comingSoon ? <Chip label={PROJECT_MESSAGES.comingSoon} size="small" variant="outlined" /> : null}
          {area.href && !area.comingSoon ? <ChevronRightIcon color="action" fontSize="small" /> : null}
        </Stack>
        <Typography color="text.secondary" variant="body2">
          {area.description}
        </Typography>
      </Stack>
    </Stack>
  )
}
