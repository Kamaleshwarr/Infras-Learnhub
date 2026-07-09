import type { ReactNode } from 'react'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import HubOutlinedIcon from '@mui/icons-material/HubOutlined'
import StorageOutlinedIcon from '@mui/icons-material/StorageOutlined'
import { Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import { PROJECT_MESSAGES } from './projectMessages'

interface ProjectAreaItem {
  icon: ReactNode
  title: string
  description: string
}

const FUTURE_AREAS: ProjectAreaItem[] = [
  {
    icon: <FolderOpenOutlinedIcon color="action" />,
    title: PROJECT_MESSAGES.knowledgeBase,
    description: 'Organized folders for requirements, documentation, and operational links.',
  },
  {
    icon: <HubOutlinedIcon color="action" />,
    title: PROJECT_MESSAGES.environments,
    description: 'Environment URLs for QA, UAT, production, and custom targets.',
  },
  {
    icon: <StorageOutlinedIcon color="action" />,
    title: PROJECT_MESSAGES.repositories,
    description: 'Git repositories and source code entry points.',
  },
  {
    icon: <GroupsOutlinedIcon color="action" />,
    title: PROJECT_MESSAGES.teamContacts,
    description: 'Expanded team directory and contact context.',
  },
]

export function ProjectAreasPanel() {
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
            {FUTURE_AREAS.map((area) => (
              <Card key={area.title} sx={{ bgcolor: 'action.hover' }} variant="outlined">
                <CardContent>
                  <Stack direction="row" spacing={2} sx={{ alignItems: 'flex-start' }}>
                    {area.icon}
                    <Stack spacing={0.5} sx={{ flex: 1 }}>
                      <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                        <Typography variant="subtitle1">{area.title}</Typography>
                        <Chip label={PROJECT_MESSAGES.comingSoon} size="small" variant="outlined" />
                      </Stack>
                      <Typography color="text.secondary" variant="body2">
                        {area.description}
                      </Typography>
                    </Stack>
                  </Stack>
                </CardContent>
              </Card>
            ))}
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  )
}
