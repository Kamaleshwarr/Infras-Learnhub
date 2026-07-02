import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import { InputAdornment, TextField } from '@mui/material'
import { LEARN_MESSAGES } from './learnMessages'

interface TechnologySearchBarProps {
  value: string
  onChange: (value: string) => void
}

export function TechnologySearchBar({ value, onChange }: TechnologySearchBarProps) {
  return (
    <TextField
      fullWidth
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <SearchOutlinedIcon />
            </InputAdornment>
          ),
        },
      }}
      onChange={(event) => onChange(event.target.value)}
      placeholder={LEARN_MESSAGES.searchPlaceholder}
      value={value}
    />
  )
}
