import {
  FormHelperText,
  MenuItem,
  Stack,
  TextField,
} from '@mui/material'
import type { InitiativeFormFieldName, InitiativeFormValues } from './initiativeFormState'
import { INITIATIVE_FORM_LIMITS, INITIATIVE_STATUS_OPTIONS } from './initiativeFormState'
import { INITIATIVE_MESSAGES } from './initiativeMessages'

interface InitiativeFormFieldsProps {
  values: InitiativeFormValues
  fieldErrors?: Partial<Record<InitiativeFormFieldName, string>>
  disabled?: boolean
  minStartDate?: string
  minExpiryDate?: string
  showStatusHelper?: boolean
  onChange: <K extends InitiativeFormFieldName>(field: K, value: InitiativeFormValues[K]) => void
}

const STATUS_LABELS = {
  ACTIVE: 'Active',
  DRAFT: 'Draft',
  EXPIRED: 'Expired',
} as const

export function InitiativeFormFields({
  values,
  fieldErrors = {},
  disabled = false,
  minStartDate,
  minExpiryDate,
  showStatusHelper = false,
  onChange,
}: InitiativeFormFieldsProps) {
  return (
    <Stack spacing={2.5}>
      <TextField
        disabled={disabled}
        error={Boolean(fieldErrors.title)}
        fullWidth
        helperText={fieldErrors.title}
        label={INITIATIVE_MESSAGES.formTitle}
        onChange={(event) => onChange('title', event.target.value)}
        required
        slotProps={{ htmlInput: { maxLength: INITIATIVE_FORM_LIMITS.title } }}
        value={values.title}
      />

      <TextField
        disabled={disabled}
        error={Boolean(fieldErrors.description)}
        fullWidth
        helperText={fieldErrors.description}
        label={INITIATIVE_MESSAGES.formDescription}
        minRows={4}
        multiline
        onChange={(event) => onChange('description', event.target.value)}
        required
        slotProps={{ htmlInput: { maxLength: INITIATIVE_FORM_LIMITS.description } }}
        value={values.description}
      />

      <TextField
        disabled={disabled}
        error={Boolean(fieldErrors.rewardDescription)}
        fullWidth
        helperText={fieldErrors.rewardDescription ?? INITIATIVE_MESSAGES.formRewardHelper}
        label={INITIATIVE_MESSAGES.formReward}
        minRows={2}
        multiline
        onChange={(event) => onChange('rewardDescription', event.target.value)}
        slotProps={{ htmlInput: { maxLength: INITIATIVE_FORM_LIMITS.rewardDescription } }}
        value={values.rewardDescription}
      />

      <TextField
        disabled={disabled}
        error={Boolean(fieldErrors.startDate)}
        fullWidth
        helperText={fieldErrors.startDate ?? INITIATIVE_MESSAGES.formUtcDateHelper}
        label={INITIATIVE_MESSAGES.formStartDate}
        onChange={(event) => onChange('startDate', event.target.value)}
        required
        slotProps={{
          htmlInput: { min: minStartDate },
          inputLabel: { shrink: true },
        }}
        type="date"
        value={values.startDate}
      />

      <TextField
        disabled={disabled}
        error={Boolean(fieldErrors.expiryDate)}
        fullWidth
        helperText={fieldErrors.expiryDate ?? INITIATIVE_MESSAGES.formUtcDateHelper}
        label={INITIATIVE_MESSAGES.formExpiryDate}
        onChange={(event) => onChange('expiryDate', event.target.value)}
        required
        slotProps={{
          htmlInput: { min: (minExpiryDate ?? values.startDate) || undefined },
          inputLabel: { shrink: true },
        }}
        type="date"
        value={values.expiryDate}
      />

      <TextField
        disabled={disabled}
        fullWidth
        label={INITIATIVE_MESSAGES.formStatus}
        onChange={(event) => onChange('status', event.target.value as InitiativeFormValues['status'])}
        required
        select
        value={values.status}
      >
        {INITIATIVE_STATUS_OPTIONS.map((status) => (
          <MenuItem key={status} value={status}>
            {STATUS_LABELS[status]}
          </MenuItem>
        ))}
      </TextField>

      {showStatusHelper ? (
        <FormHelperText>{INITIATIVE_MESSAGES.formStatusHelper}</FormHelperText>
      ) : null}
    </Stack>
  )
}
