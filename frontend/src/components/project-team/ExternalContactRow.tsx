import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import LaunchIcon from '@mui/icons-material/Launch'
import StarIcon from '@mui/icons-material/Star'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import { Box, Chip, IconButton, Stack, Tooltip, Typography } from '@mui/material'
import { descriptionClampSx } from '../common/cardLayoutStyles'
import type { ProjectExternalContact } from '../../types/projectTeam'
import { EXTERNAL_CONTACT_TYPE_LABELS } from '../../types/projectTeam'
import { TEAM_MESSAGES } from './teamMessages'

interface ExternalContactRowProps {
  contact: ProjectExternalContact
  canManage: boolean
  onEdit: (contact: ProjectExternalContact) => void
  onRemove: (contact: ProjectExternalContact) => void
}

export function ExternalContactRow({ canManage, contact, onEdit, onRemove }: ExternalContactRowProps) {
  const subtitle = [contact.roleTitle, contact.organization].filter(Boolean).join(' · ')

  return (
    <Box
      sx={{
        alignItems: 'flex-start',
        borderBottom: '1px solid',
        borderColor: 'divider',
        display: 'grid',
        gap: 1.5,
        gridTemplateColumns: { xs: '1fr', sm: '1fr auto' },
        minWidth: 0,
        py: 1.5,
        width: '100%',
      }}
    >
      <Stack spacing={0.5} sx={{ minWidth: 0 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <Typography variant="subtitle2">{contact.name}</Typography>
          {contact.primaryContact ? (
            <Chip
              color="warning"
              icon={<StarIcon />}
              label={TEAM_MESSAGES.primaryContact}
              size="small"
              variant="outlined"
            />
          ) : null}
        </Stack>
        <Typography color="text.secondary" variant="body2">
          {EXTERNAL_CONTACT_TYPE_LABELS[contact.contactType]}
          {subtitle ? ` · ${subtitle}` : ''}
        </Typography>
        {contact.notes ? (
          <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
            {contact.notes}
          </Typography>
        ) : null}
        <Stack direction="row" spacing={0.5} sx={{ flexWrap: 'wrap' }}>
          {contact.email ? (
            <Tooltip title={`Email ${contact.email}`}>
              <IconButton aria-label={`Email ${contact.name}`} component="a" href={`mailto:${contact.email}`} size="small">
                <EmailOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : null}
          {contact.contactUrl ? (
            <Tooltip title="Open contact URL">
              <IconButton
                aria-label={`Open contact URL for ${contact.name}`}
                component="a"
                href={contact.contactUrl}
                rel="noopener noreferrer"
                size="small"
                target="_blank"
              >
                <LaunchIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : null}
        </Stack>
      </Stack>
      {canManage ? (
        <Stack direction="row" spacing={0.5}>
          <Tooltip title={TEAM_MESSAGES.editExternalContact}>
            <IconButton aria-label={`Edit ${contact.name}`} onClick={() => onEdit(contact)} size="small">
              <EditOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={TEAM_MESSAGES.removeExternalContact}>
            <IconButton aria-label={`Remove ${contact.name}`} onClick={() => onRemove(contact)} size="small">
              <DeleteOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ) : null}
    </Box>
  )
}
