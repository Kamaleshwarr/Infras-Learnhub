import { useMemo, useRef, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormHelperText,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import type { InitiativeSummary } from '../../api/initiativesApi'
import { resolveApiError } from '../../utils/apiErrors'
import { getCertificateAcceptAttribute, isAllowedCertificateFile } from './certificateFileValidation'
import { normalizeInitiativeId, sortInitiativesForSubmitDropdown } from './submissionInitiativeFilter'
import { MAX_SUBMISSION_COMMENTS_LENGTH, SUBMISSION_MESSAGES } from './submissionMessages'

export interface SubmitCertificateValues {
  initiativeId: string
  file: File
  comments: string
}

interface SubmitCertificateFormProps {
  initiatives: InitiativeSummary[]
  submittedInitiativeIds: Set<string>
  loadingInitiatives: boolean
  loadError: string | null
  emptyMessage: string | null
  infoMessage: string | null
  submitting: boolean
  onSubmit: (values: SubmitCertificateValues) => Promise<void>
}

interface FieldErrors {
  initiativeId?: string
  file?: string
  comments?: string
}

const EMPTY_FORM = {
  initiativeId: '',
  comments: '',
}

export function SubmitCertificateForm({
  initiatives,
  submittedInitiativeIds,
  loadingInitiatives,
  loadError,
  emptyMessage,
  infoMessage,
  submitting,
  onSubmit,
}: SubmitCertificateFormProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [formError, setFormError] = useState<string | null>(null)

  const commentsLength = form.comments.length
  const formDisabled = loadingInitiatives || submitting || Boolean(loadError) || Boolean(emptyMessage)
  const selectableInitiatives = initiatives.filter(
    (initiative) => !submittedInitiativeIds.has(normalizeInitiativeId(initiative.id)),
  )
  const dropdownInitiatives = useMemo(
    () => sortInitiativesForSubmitDropdown(initiatives, submittedInitiativeIds),
    [initiatives, submittedInitiativeIds],
  )

  function updateField<K extends keyof typeof form>(field: K, value: (typeof form)[K]) {
    setForm((current) => ({ ...current, [field]: value }))
    setFieldErrors((current) => {
      if (!(field in current)) {
        return current
      }
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null
    event.target.value = ''
    setSelectedFile(file)
    setFieldErrors((current) => {
      if (!current.file) {
        return current
      }
      const next = { ...current }
      delete next.file
      return next
    })
  }

  function validateForm(): FieldErrors {
    const errors: FieldErrors = {}

    if (!form.initiativeId) {
      errors.initiativeId = SUBMISSION_MESSAGES.initiativeRequired
    } else if (submittedInitiativeIds.has(normalizeInitiativeId(form.initiativeId))) {
      errors.initiativeId = SUBMISSION_MESSAGES.duplicateSubmission
    }

    if (!selectedFile) {
      errors.file = SUBMISSION_MESSAGES.fileRequired
    } else if (!isAllowedCertificateFile(selectedFile)) {
      errors.file = SUBMISSION_MESSAGES.fileValidationError
    }

    if (form.comments.length > MAX_SUBMISSION_COMMENTS_LENGTH) {
      errors.comments = `Comments must be ${MAX_SUBMISSION_COMMENTS_LENGTH} characters or fewer.`
    }

    return errors
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFormError(null)

    const errors = validateForm()
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      return
    }

    if (!selectedFile) {
      return
    }

    try {
      await onSubmit({
        initiativeId: form.initiativeId,
        file: selectedFile,
        comments: form.comments.trim(),
      })
    } catch (error) {
      const message = resolveApiError(error, SUBMISSION_MESSAGES.submitError)
      setFormError(
        message.includes('already exists') ? SUBMISSION_MESSAGES.duplicateSubmission : message,
      )
    }
  }

  return (
    <Paper sx={{ p: 3 }}>
      {loadError ? <Alert severity="error" sx={{ mb: 2 }}>{loadError}</Alert> : null}
      {emptyMessage ? <Alert severity="info" sx={{ mb: 2 }}>{emptyMessage}</Alert> : null}
      {infoMessage ? <Alert severity="info" sx={{ mb: 2 }}>{infoMessage}</Alert> : null}
      {formError ? <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert> : null}

      <Box component="form" noValidate onSubmit={(event) => void handleSubmit(event)}>
        <Stack spacing={3}>
          <TextField
            disabled={formDisabled || initiatives.length === 0}
            error={Boolean(fieldErrors.initiativeId)}
            fullWidth
            helperText={fieldErrors.initiativeId}
            label="Initiative"
            onChange={(event) => updateField('initiativeId', event.target.value)}
            required
            select
            value={form.initiativeId}
          >
            {dropdownInitiatives.map((initiative) => {
              const alreadySubmitted = submittedInitiativeIds.has(normalizeInitiativeId(initiative.id))
              return (
                <MenuItem key={initiative.id} disabled={alreadySubmitted} value={initiative.id}>
                  {alreadySubmitted ? `${initiative.title} (already submitted)` : initiative.title}
                </MenuItem>
              )
            })}
          </TextField>

          <Box>
            <input
              accept={getCertificateAcceptAttribute()}
              hidden
              onChange={handleFileChange}
              ref={fileInputRef}
              type="file"
            />
            <Stack spacing={1}>
              <Typography component="label" variant="subtitle2">
                Certificate file
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ alignItems: 'center' }}>
                <Button
                  disabled={formDisabled}
                  onClick={() => fileInputRef.current?.click()}
                  startIcon={submitting ? <CircularProgress color="inherit" size={18} /> : <UploadFileOutlinedIcon />}
                  variant="outlined"
                >
                  {selectedFile ? 'Replace file' : 'Choose file'}
                </Button>
                <Typography color="text.secondary" variant="body2">
                  {selectedFile ? selectedFile.name : 'PDF, JPEG, or PNG up to 25 MB'}
                </Typography>
              </Stack>
              {fieldErrors.file ? <FormHelperText error>{fieldErrors.file}</FormHelperText> : null}
            </Stack>
          </Box>

          <TextField
            disabled={formDisabled}
            error={Boolean(fieldErrors.comments)}
            fullWidth
            helperText={
              fieldErrors.comments ?? `${commentsLength}/${MAX_SUBMISSION_COMMENTS_LENGTH} characters`
            }
            label="Comments"
            minRows={3}
            multiline
            onChange={(event) => updateField('comments', event.target.value)}
            placeholder="Optional notes about your certificate submission"
            value={form.comments}
          />

          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button
              disabled={formDisabled || selectableInitiatives.length === 0 || submitting}
              startIcon={submitting ? <CircularProgress color="inherit" size={18} /> : undefined}
              type="submit"
              variant="contained"
            >
              Submit certificate
            </Button>
          </Box>
        </Stack>
      </Box>
    </Paper>
  )
}
