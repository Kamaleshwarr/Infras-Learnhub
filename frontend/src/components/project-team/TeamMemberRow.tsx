import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined'
import EditOutlinedIcon from '@mui/icons-material/EditOutlined'
import StarIcon from '@mui/icons-material/Star'
import DeleteOutlinedIcon from '@mui/icons-material/DeleteOutlined'
import {
  Avatar,
  Box,
  Chip,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material'
import { descriptionClampSx } from '../common/cardLayoutStyles'
import type { ProjectMember } from '../../types/projects'
import { PROJECT_ROLE_LABELS } from '../../types/projects'
import { PROJECT_FUNCTIONAL_ROLE_LABELS } from '../../types/projectTeam'
import { TEAM_MESSAGES } from './teamMessages'

interface TeamMemberRowProps {
  member: ProjectMember
  canManage: boolean
  onEdit: (member: ProjectMember) => void
  onRemove: (member: ProjectMember) => void
}

function getInitials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}

export function TeamMemberRow({ canManage, member, onEdit, onRemove }: TeamMemberRowProps) {
  return (
    <Box
      sx={{
        alignItems: 'flex-start',
        borderBottom: '1px solid',
        borderColor: 'divider',
        display: 'grid',
        gap: 1.5,
        gridTemplateColumns: { xs: 'auto 1fr', sm: 'auto 1fr auto' },
        minWidth: 0,
        py: 1.5,
        width: '100%',
      }}
    >
      <Avatar sx={{ bgcolor: 'primary.main', height: 40, width: 40 }}>{getInitials(member.user.fullName)}</Avatar>
      <Stack spacing={0.5} sx={{ minWidth: 0 }}>
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
          <Typography variant="subtitle2">{member.user.fullName}</Typography>
          {member.primaryContact ? (
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
          {PROJECT_FUNCTIONAL_ROLE_LABELS[member.functionalRole]}
        </Typography>
        {member.responsibility ? (
          <Typography color="text.secondary" sx={descriptionClampSx} variant="body2">
            {member.responsibility}
          </Typography>
        ) : (
          <Typography color="text.disabled" variant="caption">
            {TEAM_MESSAGES.noResponsibility}
          </Typography>
        )}
        <Stack direction="row" spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap', pt: 0.25 }}>
          <Chip label={PROJECT_ROLE_LABELS[member.projectRole]} size="small" variant="outlined" />
          {member.user.email ? (
            <Tooltip title={`Email ${member.user.email}`}>
              <IconButton
                aria-label={`Email ${member.user.fullName}`}
                component="a"
                href={`mailto:${member.user.email}`}
                size="small"
              >
                <EmailOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          ) : null}
        </Stack>
      </Stack>
      {canManage ? (
        <Stack direction="row" spacing={0.5} sx={{ justifyContent: { xs: 'flex-start', sm: 'flex-end' } }}>
          <Tooltip title={TEAM_MESSAGES.editMember}>
            <IconButton aria-label={`Edit ${member.user.fullName}`} onClick={() => onEdit(member)} size="small">
              <EditOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title={TEAM_MESSAGES.removeMember}>
            <IconButton aria-label={`Remove ${member.user.fullName}`} onClick={() => onRemove(member)} size="small">
              <DeleteOutlinedIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ) : null}
    </Box>
  )
}
