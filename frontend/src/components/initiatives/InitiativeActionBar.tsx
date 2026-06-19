import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import { Button, Stack } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import type { CertificateSubmission } from '../../types/submissions'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeActionBarProps {
  initiativeId: string
  submission: CertificateSubmission | null
  loading: boolean
}

export function InitiativeActionBar({ initiativeId, submission, loading }: InitiativeActionBarProps) {
  const hasSubmission = submission != null
  const canSubmit = !hasSubmission

  return (
    <Stack direction={{ sm: 'row', xs: 'column' }} spacing={1}>
      {canSubmit ? (
        <Button
          component={RouterLink}
          disabled={loading}
          startIcon={<UploadFileOutlinedIcon />}
          to={`/submissions/new?initiativeId=${encodeURIComponent(initiativeId)}`}
          variant="contained"
        >
          {INITIATIVE_MESSAGES.submitCertificate}
        </Button>
      ) : null}
      {hasSubmission ? (
        <Button component={RouterLink} disabled={loading} to="/submissions" variant="outlined">
          {INITIATIVE_MESSAGES.viewMySubmission}
        </Button>
      ) : null}
    </Stack>
  )
}
