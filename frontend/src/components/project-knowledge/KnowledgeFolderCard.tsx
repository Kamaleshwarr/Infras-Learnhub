import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined'
import { Card, CardActionArea, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import {
  descriptionClampSx,
  flexCardContentSx,
  flexCardSx,
} from '../common/cardLayoutStyles'
import { longTextWrapSx } from '../common/textStyles'
import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'

interface KnowledgeFolderCardProps {
  folder: ProjectKnowledgeFolder
  href: string
}

export function KnowledgeFolderCard({ folder, href }: KnowledgeFolderCardProps) {
  const description = folder.description?.trim()
  const summary =
    [
      folder.childFolderCount > 0 ? KNOWLEDGE_MESSAGES.subfolderCount(folder.childFolderCount) : null,
      folder.itemCount > 0 ? KNOWLEDGE_MESSAGES.resourceCount(folder.itemCount) : null,
    ]
      .filter(Boolean)
      .join(' · ') || 'No resources yet'

  return (
    <Card sx={flexCardSx} variant="outlined">
      <CardActionArea component={RouterLink} sx={{ ...flexCardSx, flexGrow: 1 }} to={href}>
        <CardContent sx={{ ...flexCardContentSx, gap: 1 }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start' }}>
            <FolderOutlinedIcon color="primary" fontSize="small" sx={{ flexShrink: 0, mt: 0.25 }} />
            <Typography sx={{ ...longTextWrapSx, flex: 1 }} variant="subtitle1">
              {folder.name}
            </Typography>
          </Stack>
          {description ? (
            <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
              {description}
            </Typography>
          ) : null}
          <Typography color="text.secondary" sx={{ mt: 'auto', pt: description ? 0.5 : 0 }} variant="caption">
            {summary}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
