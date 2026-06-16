import { Alert, Box, Paper, Typography } from '@mui/material'
import type { SubmitCertificateDiagnostics } from './submitCertificateDiagnostics'

interface SubmitCertificateDiagnosticsPanelProps {
  diagnostics: SubmitCertificateDiagnostics
}

export function SubmitCertificateDiagnosticsPanel({ diagnostics }: SubmitCertificateDiagnosticsPanelProps) {
  return (
    <Paper sx={{ mb: 3, p: 2, bgcolor: 'grey.50', border: '1px dashed', borderColor: 'warning.main' }}>
      <Alert severity="warning" sx={{ mb: 2 }}>
        Temporary Phase 1 diagnostics — remove before release. Open the browser console for the same payload under
        [SubmitCertificateDiagnostics].
      </Alert>

      <Typography gutterBottom variant="subtitle1">
        Submit Certificate diagnostics
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 2 }} variant="body2">
        Captured at {diagnostics.capturedAtUtc}
      </Typography>

      <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
        {JSON.stringify(diagnostics, null, 2)}
      </Box>
    </Paper>
  )
}
