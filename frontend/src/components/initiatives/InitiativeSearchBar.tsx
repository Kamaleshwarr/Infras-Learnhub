import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import { InputAdornment, TextField } from '@mui/material'
import { INITIATIVE_FORM_LIMITS } from './initiativeFormState'

interface InitiativeSearchBarProps {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
}

export function InitiativeSearchBar({ value, onChange, disabled = false }: InitiativeSearchBarProps) {
  return (
    <TextField
      disabled={disabled}
      fullWidth
      label="Search initiatives"
      onChange={(event) => onChange(event.target.value)}
      placeholder="Search by title"
      slotProps={{
        htmlInput: { maxLength: INITIATIVE_FORM_LIMITS.title },
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <SearchOutlinedIcon color="action" />
            </InputAdornment>
          ),
        },
      }}
      value={value}
    />
  )
}
