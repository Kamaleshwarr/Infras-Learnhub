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
import type { ProjectKnowledgeItem } from '../../types/projectKnowledge'
import { KNOWLEDGE_CATEGORY_LABELS } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'
import { WrappingText } from '../common/WrappingText'

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

  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent>
        <Stack spacing={1.5} sx={{ height: '100%' }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <Stack spacing={0.5} sx={{ flex: 1, minWidth: 0 }}>
              <Typography variant="subtitle1">{item.title}</Typography>
              <Stack direction="row" spacing={1} sx={{ flexWrap: 'wrap' }}>
                <Chip label={KNOWLEDGE_CATEGORY_LABELS[item.category]} size="small" variant="outlined" />
                {item.sourceType === 'LINK' ? (
                  <Chip color="info" label={KNOWLEDGE_MESSAGES.externalLink} size="small" variant="outlined" />
                ) : null}
              </Stack>
            </Stack>
            <Stack direction="row" spacing={0.5}>
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
          {item.description ? (
            <Typography color="text.secondary" variant="body2">
              <WrappingText>{item.description}</WrappingText>
            </Typography>
          ) : null}
          {host ? (
            <Typography color="text.secondary" variant="caption">
              {host}
            </Typography>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
