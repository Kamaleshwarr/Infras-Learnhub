import { Alert, Stack, Typography } from '@mui/material'
import type { CatalogStatus } from '../../types/learn'
import { LEARN_MESSAGES } from './learnMessages'

interface TechnologyListToolbarProps {
  catalogStatus: CatalogStatus | null
}

export function TechnologyListToolbar({ catalogStatus }: TechnologyListToolbarProps) {
  return (
    <Stack spacing={1} sx={{ mb: 2 }}>
      <Typography color="text.secondary" variant="body2">
        {LEARN_MESSAGES.manageCatalogDescription}
      </Typography>
      {catalogStatus?.catalogVersion ? (
        <Alert severity="info">
          {LEARN_MESSAGES.catalogStatusLabel}: {catalogStatus.catalogVersion} ·{' '}
          {catalogStatus.technologyCount} {LEARN_MESSAGES.catalogTechnologiesLabel}
        </Alert>
      ) : null}
    </Stack>
  )
}
