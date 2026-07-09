import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  Card,
  CardContent,
  Chip,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material'
import {
  descriptionClampSx,
  flexCardBodySx,
  flexCardContentSx,
  flexCardSx,
} from '../common/cardLayoutStyles'
import { longTextWrapSx } from '../common/textStyles'
import type { ProjectKnowledgeItem } from '../../types/projectKnowledge'
import { KNOWLEDGE_CATEGORY_LABELS } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'

function formatExternalHost(url?: string | null) {
  if (!url) {
    return null
  }
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return null
  }
}

interface KnowledgeResourceCardProps {
  item: ProjectKnowledgeItem
  canManage: boolean
  onOpen: (item: ProjectKnowledgeItem) => void
  onEdit?: (item: ProjectKnowledgeItem) => void
  onDelete?: (item: ProjectKnowledgeItem) => void
}

export function KnowledgeResourceCard({ canManage, item, onDelete, onEdit, onOpen }: KnowledgeResourceCardProps) {
  const host = formatExternalHost(item.externalUrl)
  const description = item.description?.trim()

  return (
    <Card sx={flexCardSx} variant="outlined">
      <CardContent sx={{ ...flexCardContentSx, gap: 1.5 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <Stack spacing={0.5} sx={{ flex: 1, minWidth: 0 }}>
            <Typography sx={longTextWrapSx} variant="subtitle1">
              {item.title}
            </Typography>
            <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
              <Chip label={KNOWLEDGE_CATEGORY_LABELS[item.category]} size="small" variant="outlined" />
              {item.sourceType === 'LINK' ? (
                <Chip color="info" label={KNOWLEDGE_MESSAGES.externalLink} size="small" variant="outlined" />
              ) : null}
            </Stack>
          </Stack>
          <Stack direction="row" spacing={0.5} sx={{ flexShrink: 0 }}>
            <Tooltip title={KNOWLEDGE_MESSAGES.openResource}>
              <IconButton aria-label={KNOWLEDGE_MESSAGES.openResource} onClick={() => onOpen(item)} size="small">
                <OpenInNewIcon fontSize="small" />
              </IconButton>
            </Tooltip>
            {canManage && onEdit ? (
              <Tooltip title={KNOWLEDGE_MESSAGES.editResource}>
                <IconButton aria-label={KNOWLEDGE_MESSAGES.editResource} onClick={() => onEdit(item)} size="small">
                  <EditOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            ) : null}
            {canManage && onDelete ? (
              <Tooltip title={KNOWLEDGE_MESSAGES.deleteResource}>
                <IconButton aria-label={KNOWLEDGE_MESSAGES.deleteResource} onClick={() => onDelete(item)} size="small">
                  <DeleteOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            ) : null}
          </Stack>
        </Stack>

        <Stack sx={flexCardBodySx}>
          {description ? (
            <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
              {description}
            </Typography>
          ) : null}
          {host ? (
            <Typography color="text.secondary" sx={{ ...longTextWrapSx, mt: description ? 0 : 'auto' }} variant="caption">
              {host}
            </Typography>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
