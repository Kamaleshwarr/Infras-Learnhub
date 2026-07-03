import { Button, FormControl, InputLabel, MenuItem, Select, Stack } from '@mui/material'
import type { TechnologyCategory, TechnologyDifficulty, TechnologyListQuery } from '../../types/learn'
import { TECHNOLOGY_CATEGORY_OPTIONS, TECHNOLOGY_DIFFICULTY_OPTIONS } from '../../types/learn'
import { LEARN_MESSAGES } from './learnMessages'

interface TechnologyFilterBarProps {
  query: TechnologyListQuery
  onChange: (nextQuery: TechnologyListQuery) => void
}

export function TechnologyFilterBar({ query, onChange }: TechnologyFilterBarProps) {
  const hasFilters = Boolean(query.category || query.difficulty)

  return (
    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { xs: 'stretch', md: 'center' }, mb: 2 }}>
      <FormControl fullWidth>
        <InputLabel id="technology-category-filter">Category</InputLabel>
        <Select
          label="Category"
          labelId="technology-category-filter"
          onChange={(event) =>
            onChange({ ...query, page: 0, category: event.target.value as TechnologyCategory | '' })
          }
          value={query.category}
        >
          <MenuItem value="">All categories</MenuItem>
          {TECHNOLOGY_CATEGORY_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl fullWidth>
        <InputLabel id="technology-difficulty-filter">Difficulty</InputLabel>
        <Select
          label="Difficulty"
          labelId="technology-difficulty-filter"
          onChange={(event) =>
            onChange({ ...query, page: 0, difficulty: event.target.value as TechnologyDifficulty | '' })
          }
          value={query.difficulty}
        >
          <MenuItem value="">All difficulties</MenuItem>
          {TECHNOLOGY_DIFFICULTY_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      {hasFilters ? (
        <Button onClick={() => onChange({ ...query, page: 0, category: '', difficulty: '' })}>
          {LEARN_MESSAGES.clearFilters}
        </Button>
      ) : null}
    </Stack>
  )
}
