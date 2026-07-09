import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined'
import { Card, CardActionArea, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { ProjectKnowledgeFolder } from '../../types/projectKnowledge'
import { KNOWLEDGE_MESSAGES } from './knowledgeMessages'

interface KnowledgeFolderCardProps {
  folder: ProjectKnowledgeFolder
  href: string
}

export function KnowledgeFolderCard({ folder, href }: KnowledgeFolderCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardActionArea component={RouterLink} sx={{ height: '100%' }} to={href}>
        <CardContent>
          <Stack spacing={1}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start' }}>
              <FolderOutlinedIcon color="primary" fontSize="small" sx={{ mt: 0.25 }} />
              <Typography sx={{ flex: 1 }} variant="subtitle1">
                {folder.name}
              </Typography>
            </Stack>
            {folder.description ? (
              <Typography color="text.secondary" variant="body2">
                {folder.description}
              </Typography>
            ) : null}
            <Typography color="text.secondary" variant="caption">
              {[
                folder.childFolderCount > 0 ? KNOWLEDGE_MESSAGES.subfolderCount(folder.childFolderCount) : null,
                folder.itemCount > 0 ? KNOWLEDGE_MESSAGES.resourceCount(folder.itemCount) : null,
              ]
                .filter(Boolean)
                .join(' · ') || 'No resources yet'}
            </Typography>
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  )
}
