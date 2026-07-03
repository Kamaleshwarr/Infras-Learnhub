import Chip from '@mui/material/Chip'
import type { TechnologyCategory, TechnologyDifficulty } from '../../types/learn'
import { TECHNOLOGY_CATEGORY_OPTIONS, TECHNOLOGY_DIFFICULTY_OPTIONS } from '../../types/learn'

export function TechnologyCategoryChip({ category }: { category: TechnologyCategory }) {
  const label = TECHNOLOGY_CATEGORY_OPTIONS.find((option) => option.value === category)?.label ?? category
  return <Chip label={label} size="small" variant="outlined" />
}

export function TechnologyDifficultyChip({ difficulty }: { difficulty: TechnologyDifficulty }) {
  const label = TECHNOLOGY_DIFFICULTY_OPTIONS.find((option) => option.value === difficulty)?.label ?? difficulty
  return <Chip color="info" label={label} size="small" variant="outlined" />
}
