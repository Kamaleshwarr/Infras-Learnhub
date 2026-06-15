import { useEffect, useRef, useState } from 'react'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from '@mui/material'
import { usersApi } from '../../api/usersApi'
import type { UserImportResponse } from '../../types/users'
import { resolveApiError } from '../../utils/apiErrors'
import { estimateImportRowCount, isAcceptedImportFile } from '../../utils/userImportPreview'

interface BulkImportDialogProps {
  open: boolean
  onClose: () => void
  onComplete: (result: UserImportResponse) => void
  onDownloadTemplate: () => void
}

type BulkImportStep = 'select' | 'preview' | 'results'

export function BulkImportDialog({ open, onClose, onComplete, onDownloadTemplate }: BulkImportDialogProps) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [step, setStep] = useState<BulkImportStep>('select')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [estimatedRows, setEstimatedRows] = useState<number | null>(null)
  const [previewLoading, setPreviewLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [result, setResult] = useState<UserImportResponse | null>(null)

  const controlsDisabled = previewLoading || submitting

  useEffect(() => {
    if (open) {
      setStep('select')
      setSelectedFile(null)
      setEstimatedRows(null)
      setPreviewLoading(false)
      setSubmitting(false)
      setFormError(null)
      setResult(null)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }, [open])

  async function handleFileSelected(fileList: FileList | null) {
    const file = fileList?.[0]
    if (!file) {
      return
    }

    setFormError(null)

    if (!isAcceptedImportFile(file)) {
      setFormError('Unsupported file format. Upload a CSV, XLS, or XLSX file.')
      setSelectedFile(null)
      setEstimatedRows(null)
      setStep('select')
      return
    }

    setPreviewLoading(true)
    setSelectedFile(file)

    try {
      const rowCount = await estimateImportRowCount(file)
      setEstimatedRows(rowCount)
      setStep('preview')
    } catch {
      setFormError('Unable to read the selected file. Choose a different file and try again.')
      setSelectedFile(null)
      setEstimatedRows(null)
      setStep('select')
    } finally {
      setPreviewLoading(false)
    }
  }

  function handleChooseDifferentFile() {
    if (controlsDisabled) {
      return
    }
    setSelectedFile(null)
    setEstimatedRows(null)
    setFormError(null)
    setStep('select')
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  async function handleConfirmImport() {
    if (!selectedFile || submitting) {
      return
    }

    setSubmitting(true)
    setFormError(null)

    try {
      const response = await usersApi.importUsers(selectedFile)
      setResult(response)
      setStep('results')
      onComplete(response)
    } catch (error) {
      setFormError(resolveApiError(error, 'Unable to import users. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  function handleClose() {
    if (!controlsDisabled) {
      onClose()
    }
  }

  return (
    <Dialog
      aria-labelledby="bulk-import-title"
      fullWidth
      maxWidth="sm"
      onClose={controlsDisabled ? undefined : handleClose}
      open={open}
    >
      <DialogTitle id="bulk-import-title">Import users</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {formError ? <Alert severity="error">{formError}</Alert> : null}
          <Alert severity="info">
            Imported users receive the temporary password <strong>Temp@12345</strong> and must change
            it on first sign-in.
          </Alert>
          <input
            accept=".csv,.xls,.xlsx"
            disabled={controlsDisabled}
            hidden
            id="bulk-import-file-input"
            onChange={(event) => void handleFileSelected(event.target.files)}
            ref={fileInputRef}
            type="file"
          />

          {step === 'select' ? (
            <>
              <Typography color="text.secondary" variant="body2">
                Upload a CSV, XLS, or XLSX file with columns: Employee ID, Full Name, Email, Role.
              </Typography>
              <Box>
                <label htmlFor="bulk-import-file-input">
                  <Button component="span" disabled={controlsDisabled} variant="outlined">
                    {previewLoading ? 'Reading file…' : 'Select file'}
                  </Button>
                </label>
              </Box>
              <Button disabled={controlsDisabled} onClick={onDownloadTemplate} size="small">
                Download template
              </Button>
            </>
          ) : null}

          {step === 'preview' && selectedFile ? (
            <>
              <Box
                sx={{
                  bgcolor: 'action.hover',
                  borderRadius: 1,
                  p: 2,
                }}
              >
                <Typography variant="subtitle2">Selected file</Typography>
                <Typography variant="body2">{selectedFile.name}</Typography>
                <Typography color="text.secondary" sx={{ mt: 1 }} variant="body2">
                  {estimatedRows === null
                    ? 'Row count will be determined during import.'
                    : estimatedRows === 0
                      ? 'No data rows detected.'
                      : `${estimatedRows} data row${estimatedRows === 1 ? '' : 's'} detected.`}
                </Typography>
              </Box>
              {estimatedRows === 0 ? (
                <Alert severity="warning">
                  The file appears to contain no importable rows. You can still continue, but no users
                  may be imported.
                </Alert>
              ) : null}
              <Typography color="text.secondary" variant="body2">
                Review the file details above, then confirm to start the import.
              </Typography>
            </>
          ) : null}

          {step === 'results' && result ? (
            <>
              <Box
                sx={{
                  bgcolor: 'action.hover',
                  borderRadius: 1,
                  p: 2,
                }}
              >
                <Typography variant="subtitle2">Import summary</Typography>
                <Typography variant="body2">Total rows: {result.totalRows}</Typography>
                <Typography variant="body2">Imported: {result.imported}</Typography>
                <Typography variant="body2">Failed: {result.failed}</Typography>
              </Box>
              {result.failed > 0 && result.imported > 0 ? (
                <Alert severity="warning">
                  Some rows were imported successfully. Review the errors below and fix the file before
                  re-importing failed rows.
                </Alert>
              ) : null}
              {result.failed > 0 && result.imported === 0 ? (
                <Alert severity="error">No users were imported. Review the errors below.</Alert>
              ) : null}
              {result.errors.length > 0 ? (
                <Box>
                  <Typography sx={{ mb: 1 }} variant="subtitle2">
                    Errors
                  </Typography>
                  <List dense sx={{ bgcolor: 'background.paper', border: 1, borderColor: 'divider', borderRadius: 1, maxHeight: 220, overflow: 'auto' }}>
                    {result.errors.map((error) => (
                      <ListItem key={error}>
                        <ListItemText primary={error} />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              ) : null}
            </>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3 }}>
        {step === 'select' ? (
          <Button disabled={controlsDisabled} onClick={handleClose}>
            Close
          </Button>
        ) : null}
        {step === 'preview' ? (
          <>
            <Button disabled={controlsDisabled} onClick={handleChooseDifferentFile}>
              Choose different file
            </Button>
            <Button disabled={controlsDisabled} onClick={handleClose}>
              Close
            </Button>
            <Button disabled={controlsDisabled || !selectedFile} onClick={() => void handleConfirmImport()} variant="contained">
              {submitting ? <CircularProgress color="inherit" size={24} /> : 'Import users'}
            </Button>
          </>
        ) : null}
        {step === 'results' ? (
          <Button disabled={controlsDisabled} onClick={handleClose} variant="contained">
            Close
          </Button>
        ) : null}
      </DialogActions>
    </Dialog>
  )
}
