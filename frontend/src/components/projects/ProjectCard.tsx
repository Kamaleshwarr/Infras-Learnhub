import ArrowForwardOutlinedIcon from '@mui/icons-material/ArrowForwardOutlined'
import GroupsOutlinedIcon from '@mui/icons-material/GroupsOutlined'
import {
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { ProjectSummary } from '../../types/projects'
import { PROJECT_ROLE_LABELS } from '../../types/projects'
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { TEXT_DISPLAY_LIMITS } from '../common/textDisplay'
import { PROJECT_MESSAGES } from './projectMessages'
import { ProjectAccessChip } from './ProjectAccessChip'
import { ProjectStatusChip } from './ProjectStatusChip'

interface ProjectCardProps {
  project: ProjectSummary
}

export function ProjectCard({ project }: ProjectCardProps) {
  const technologyPreview = (project.relatedTechnologies ?? []).slice(0, 3)

  return (
    <Card sx={{ display: 'flex', flexDirection: 'column', height: '100%' }} variant="outlined">
      <CardContent sx={{ flexGrow: 1 }}>
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start', flexWrap: 'wrap' }}>
            <Typography sx={{ flex: 1, minWidth: 0 }} variant="h6">
              <TruncatedTextWithTooltip maxLength={TEXT_DISPLAY_LIMITS.cardTitle} text={project.name} />
            </Typography>
            <ProjectStatusChip status={project.status} />
          </Stack>
          <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
            <ProjectAccessChip accessType={project.accessType} />
            {project.currentMemberRole ? (
              <Chip label={PROJECT_ROLE_LABELS[project.currentMemberRole]} size="small" variant="outlined" />
            ) : null}
          </Stack>
          {project.description ? (
            <Typography color="text.secondary" variant="body2">
              <TruncatedTextWithTooltip maxLength={TEXT_DISPLAY_LIMITS.cardReward} text={project.description} />
            </Typography>
          ) : (
            <Typography color="text.secondary" variant="body2">
              No description provided.
            </Typography>
          )}
          {technologyPreview.length > 0 ? (
            <Stack direction="row" spacing={0.75} sx={{ flexWrap: 'wrap' }}>
              {technologyPreview.map((technology) => (
                <Chip key={technology.id} label={technology.name} size="small" variant="outlined" />
              ))}
              {(project.relatedTechnologies?.length ?? 0) > technologyPreview.length ? (
                <Chip
                  label={`+${(project.relatedTechnologies?.length ?? 0) - technologyPreview.length}`}
                  size="small"
                  variant="outlined"
                />
              ) : null}
            </Stack>
          ) : null}
          <Stack direction="row" spacing={2} sx={{ color: 'text.secondary' }}>
            {project.owner ? (
              <Typography variant="caption">Owner: {project.owner.fullName}</Typography>
            ) : null}
            {project.memberCount != null ? (
              <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
                <GroupsOutlinedIcon fontSize="inherit" />
                <Typography variant="caption">{project.memberCount}</Typography>
              </Stack>
            ) : null}
          </Stack>
        </Stack>
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2, pt: 0 }}>
        <Button
          component={RouterLink}
          endIcon={<ArrowForwardOutlinedIcon />}
          fullWidth
          to={`/projects/${project.id}`}
          variant="contained"
        >
          {PROJECT_MESSAGES.openProject}
        </Button>
      </CardActions>
    </Card>
  )
}

interface ProjectCardGridProps {
  projects: ProjectSummary[]
}

export function ProjectCardGrid({ projects }: ProjectCardGridProps) {
  return (
    <Box
      sx={{
        display: 'grid',
        gap: 2,
        gridTemplateColumns: {
          xs: '1fr',
          sm: 'repeat(2, 1fr)',
          lg: 'repeat(3, 1fr)',
        },
      }}
    >
      {projects.map((project) => (
        <ProjectCard key={project.id} project={project} />
      ))}
    </Box>
  )
}
