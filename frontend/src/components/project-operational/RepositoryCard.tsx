import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LaunchIcon from '@mui/icons-material/Launch'
import { Button, Card, CardContent, Chip, Stack, Typography } from '@mui/material'
import {
  descriptionClampSx,
  flexCardActionsSx,
  flexCardBodySx,
  flexCardContentSx,
  flexCardSx,
} from '../common/cardLayoutStyles'
import { longTextWrapSx } from '../common/textStyles'
import { OPERATIONAL_MESSAGES } from './operationalMessages'
import type { ProjectLinkedRepository } from '../../types/projectOperational'
import { REPOSITORY_PROVIDER_LABELS, REPOSITORY_TYPE_LABELS } from '../../types/projectOperational'

interface RepositoryCardProps {
  repository: ProjectLinkedRepository
  canManage: boolean
  canDelete: boolean
  onEdit: (repository: ProjectLinkedRepository) => void
  onDelete: (repository: ProjectLinkedRepository) => void
}

export function RepositoryCard({ canDelete, canManage, onDelete, onEdit, repository }: RepositoryCardProps) {
  const description = repository.description?.trim()
  const defaultBranch = repository.defaultBranch?.trim()

  return (
    <Card sx={flexCardSx} variant="outlined">
      <CardContent sx={{ ...flexCardContentSx, gap: 1.5 }}>
        <Typography sx={{ ...longTextWrapSx, fontWeight: 600 }} variant="h6">
          {repository.name}
        </Typography>

        <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
          <Chip label={REPOSITORY_TYPE_LABELS[repository.repositoryType]} size="small" />
          <Chip label={REPOSITORY_PROVIDER_LABELS[repository.provider]} size="small" variant="outlined" />
        </Stack>

        <Stack sx={flexCardBodySx}>
          {description ? (
            <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
              {description}
            </Typography>
          ) : null}
          {defaultBranch ? (
            <Typography color="text.secondary" variant="body2">
              Default branch: {defaultBranch}
            </Typography>
          ) : null}
        </Stack>

        <Stack direction="row" spacing={1} sx={{ ...flexCardActionsSx, flexWrap: 'wrap' }}>
          <Button
            endIcon={<LaunchIcon />}
            href={repository.repositoryUrl}
            rel="noopener noreferrer"
            size="small"
            target="_blank"
            variant="outlined"
          >
            {OPERATIONAL_MESSAGES.openRepository}
          </Button>
          {canManage ? (
            <Button
              onClick={() => onEdit(repository)}
              size="small"
              startIcon={<EditOutlinedIcon />}
              variant="text"
            >
              Edit
            </Button>
          ) : null}
          {canDelete ? (
            <Button
              color="error"
              onClick={() => onDelete(repository)}
              size="small"
              startIcon={<DeleteOutlinedIcon />}
              variant="text"
            >
              Delete
            </Button>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
