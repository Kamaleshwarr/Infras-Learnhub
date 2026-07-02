import {
  Alert,
  Autocomplete,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Switch,
  TextField,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { projectsApi } from '../../api/projectsApi'
import type { TechnologyFormFieldName, TechnologyFormValues } from './learnFormState'
import { LEARN_MESSAGES } from './learnMessages'
import { TECHNOLOGY_CATEGORY_OPTIONS, TECHNOLOGY_DIFFICULTY_OPTIONS } from '../../types/learn'

interface TechnologyFormFieldsProps {
  form: TechnologyFormValues
  fieldErrors: Partial<Record<TechnologyFormFieldName, string>>
  onChange: <K extends TechnologyFormFieldName>(field: K, value: TechnologyFormValues[K]) => void
  showFeatured?: boolean
  showProjectLinks?: boolean
}

interface ProjectOption {
  id: string
  label: string
}

export function TechnologyFormFields({
  form,
  fieldErrors,
  onChange,
  showFeatured = false,
  showProjectLinks = false,
}: TechnologyFormFieldsProps) {
  const [projectOptions, setProjectOptions] = useState<ProjectOption[]>([])
  const [projectLoadError, setProjectLoadError] = useState<string | null>(null)

  useEffect(() => {
    if (!showProjectLinks) {
      return
    }

    let mounted = true

    async function loadProjects() {
      try {
        const response = await projectsApi.list(undefined, { size: 100, sort: 'name,asc' })
        if (mounted) {
          setProjectOptions(response.content.map((project) => ({ id: project.id, label: project.name })))
          setProjectLoadError(null)
        }
      } catch {
        if (mounted) {
          setProjectLoadError('Unable to load projects for linking.')
        }
      }
    }

    void loadProjects()

    return () => {
      mounted = false
    }
  }, [showProjectLinks])

  const selectedProjects = projectOptions.filter((option) => form.linkedProjectIds.includes(option.id))

  return (
    <Stack spacing={2}>
      <TextField
        error={Boolean(fieldErrors.name)}
        fullWidth
        helperText={fieldErrors.name}
        label={LEARN_MESSAGES.formName}
        onChange={(event) => onChange('name', event.target.value)}
        required
        value={form.name}
      />
      <TextField
        error={Boolean(fieldErrors.shortName)}
        fullWidth
        helperText={fieldErrors.shortName}
        label={LEARN_MESSAGES.formShortName}
        onChange={(event) => onChange('shortName', event.target.value)}
        required
        value={form.shortName}
      />
      <TextField
        error={Boolean(fieldErrors.description)}
        fullWidth
        helperText={fieldErrors.description}
        label={LEARN_MESSAGES.formDescription}
        minRows={4}
        multiline
        onChange={(event) => onChange('description', event.target.value)}
        value={form.description}
      />
      <FormControl error={Boolean(fieldErrors.category)} fullWidth required>
        <InputLabel id="technology-form-category">{LEARN_MESSAGES.formCategory}</InputLabel>
        <Select
          label={LEARN_MESSAGES.formCategory}
          labelId="technology-form-category"
          onChange={(event) => onChange('category', event.target.value as TechnologyFormValues['category'])}
          value={form.category}
        >
          {TECHNOLOGY_CATEGORY_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl error={Boolean(fieldErrors.difficulty)} fullWidth required>
        <InputLabel id="technology-form-difficulty">{LEARN_MESSAGES.formDifficulty}</InputLabel>
        <Select
          label={LEARN_MESSAGES.formDifficulty}
          labelId="technology-form-difficulty"
          onChange={(event) => onChange('difficulty', event.target.value as TechnologyFormValues['difficulty'])}
          value={form.difficulty}
        >
          {TECHNOLOGY_DIFFICULTY_OPTIONS.map((option) => (
            <MenuItem key={option.value} value={option.value}>
              {option.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      {showFeatured ? (
        <FormControlLabel
          control={
            <Switch
              checked={form.featured}
              onChange={(event) => onChange('featured', event.target.checked)}
            />
          }
          label={LEARN_MESSAGES.formFeatured}
        />
      ) : null}
      {showProjectLinks ? (
        <Stack spacing={1}>
          <Autocomplete
            getOptionLabel={(option) => option.label}
            isOptionEqualToValue={(option, value) => option.id === value.id}
            multiple
            onChange={(_event, value) => onChange('linkedProjectIds', value.map((item) => item.id))}
            options={projectOptions}
            renderInput={(params) => (
              <TextField
                {...params}
                helperText={LEARN_MESSAGES.formProjectLinksHelper}
                label={LEARN_MESSAGES.formProjectLinks}
              />
            )}
            value={selectedProjects}
          />
          {projectLoadError ? <Alert severity="warning">{projectLoadError}</Alert> : null}
        </Stack>
      ) : null}
    </Stack>
  )
}
