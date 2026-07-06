import SearchIcon from '@mui/icons-material/Search'
import { InputAdornment, TextField } from '@mui/material'
import { PROJECT_MESSAGES } from './projectMessages'

interface ProjectSearchBarProps {
  value: string
  onChange: (value: string) => void
}

export function ProjectSearchBar({ onChange, value }: ProjectSearchBarProps) {
  return (
    <TextField
      fullWidth
      onChange={(event) => onChange(event.target.value)}
      placeholder={PROJECT_MESSAGES.searchPlaceholder}
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon fontSize="small" />
            </InputAdornment>
          ),
        },
      }}
      value={value}
    />
  )
}
