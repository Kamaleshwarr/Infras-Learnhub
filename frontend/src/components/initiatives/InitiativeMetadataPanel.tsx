import { Box, Stack } from '@mui/material'
import type { Initiative } from '../../types/initiatives'
import { WrappingText } from '../common/WrappingText'
import { formatInitiativeTimestamp } from './initiativeDisplay'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeMetadataPanelProps {
  initiative: Initiative
}

export function InitiativeMetadataPanel({ initiative }: InitiativeMetadataPanelProps) {
  return (
    <Box
      sx={{
        bgcolor: 'action.hover',
        borderRadius: 1,
        minWidth: 0,
        p: 2,
      }}
    >
      <Stack spacing={0.5}>
        {initiative.createdBy ? (
          <WrappingText color="text.secondary" variant="body2">
            {INITIATIVE_MESSAGES.metadataCreatedBy}: {initiative.createdBy.fullName} ({initiative.createdBy.email})
          </WrappingText>
        ) : null}
        {initiative.createdAtUtc ? (
          <WrappingText color="text.secondary" variant="body2">
            {INITIATIVE_MESSAGES.metadataCreatedOn}: {formatInitiativeTimestamp(initiative.createdAtUtc)}
          </WrappingText>
        ) : null}
        {initiative.updatedAtUtc ? (
          <WrappingText color="text.secondary" variant="body2">
            {INITIATIVE_MESSAGES.metadataLastUpdated}: {formatInitiativeTimestamp(initiative.updatedAtUtc)}
          </WrappingText>
        ) : null}
      </Stack>
    </Box>
  )
}
