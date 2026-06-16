import { Alert, Box, Paper, Stack, Typography } from '@mui/material'
import type { SubmitCertificateDiagnostics } from './submitCertificateDiagnostics'

interface SubmitCertificateDiagnosticsPanelProps {
  diagnostics: SubmitCertificateDiagnostics
}

function formatValue(value: unknown) {
  return JSON.stringify(value, null, 2)
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

      <Stack spacing={2} sx={{ mb: 3 }}>
        <Box>
          <Typography variant="subtitle2">1. GET /api/v1/initiatives (as received by SubmitCertificatePage)</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {formatValue(diagnostics.rawInitiativesResponse)}
          </Box>
        </Box>

        <Box>
          <Typography variant="subtitle2">2. GET /api/v1/me/submissions (all pages loaded)</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {formatValue(diagnostics.rawSubmissionsResponse)}
          </Box>
        </Box>

        <Box>
          <Typography variant="subtitle2">3. availableInitiatives.length</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {diagnostics.availableInitiativesCount}
          </Box>
        </Box>

        <Box>
          <Typography variant="subtitle2">4. submittedInitiativeIds</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {formatValue(diagnostics.submittedInitiativeIds)}
          </Box>
        </Box>

        <Box>
          <Typography variant="subtitle2">5. Final initiative list after filtering</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {formatValue(diagnostics.availableInitiatives)}
          </Box>
        </Box>

        <Box>
          <Typography variant="subtitle2">6. Excluded initiatives and reasons</Typography>
          <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
            {formatValue({
              filteredOutAsAlreadySubmitted: diagnostics.excludedInitiatives,
              droppedDuringParsing: diagnostics.parseExclusions,
            })}
          </Box>
        </Box>
      </Stack>

      <Typography gutterBottom variant="subtitle2">
        Full diagnostics payload
      </Typography>
      <Box component="pre" sx={{ fontSize: 12, overflow: 'auto', whiteSpace: 'pre-wrap' }}>
        {formatValue(diagnostics)}
      </Box>
    </Paper>
  )
}
