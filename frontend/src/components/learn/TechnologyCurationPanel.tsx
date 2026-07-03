import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Link,
  Stack,
  Switch,
  TextField,
  Typography,
} from '@mui/material'
import { learnApi } from '../../api/learnApi'
import { projectsApi, type ProjectSummary } from '../../api/projectsApi'
import type { Technology } from '../../types/learn'
import { getValidationErrors, resolveApiError } from '../../utils/apiErrors'
import { LEARN_MESSAGES } from './learnMessages'
import { TechnologyCategoryChip, TechnologyDifficultyChip } from './TechnologyMetaChips'
import { TechnologyStatusChip } from './TechnologyStatusChip'

interface TechnologyCurationPanelProps {
  open: boolean
  technology: Technology | null
  onClose: () => void
  onSuccess: () => void
}

export function TechnologyCurationPanel({ open, technology, onClose, onSuccess }: TechnologyCurationPanelProps) {
  const [featured, setFeatured] = useState(false)
  const [orgNotes, setOrgNotes] = useState('')
  const [linkedProjectIds, setLinkedProjectIds] = useState<string[]>([])
  const [projects, setProjects] = useState<ProjectSummary[]>([])
  const [loadingProjects, setLoadingProjects] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!open || !technology) {
      return
    }

    setFeatured(technology.featured)
    setOrgNotes(technology.orgNotes ?? '')
    setLinkedProjectIds(technology.relatedProjects?.map((project) => project.id) ?? [])
    setFormError(null)
    setSubmitting(false)
  }, [open, technology])

  useEffect(() => {
    if (!open) {
      return
    }

    let mounted = true
    async function loadProjects() {
      setLoadingProjects(true)
      try {
        const response = await projectsApi.list(undefined, { page: 0, size: 100, sort: 'name,asc' })
        if (mounted) {
          setProjects(response.content)
        }
      } catch {
        if (mounted) {
          setProjects([])
        }
      } finally {
        if (mounted) {
          setLoadingProjects(false)
        }
      }
    }

    void loadProjects()
    return () => {
      mounted = false
    }
  }, [open])

  const projectOptions = useMemo(
    () => projects.map((project) => ({ id: project.id, label: project.name })),
    [projects],
  )

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!technology) {
      return
    }

    setSubmitting(true)
    setFormError(null)

    try {
      await learnApi.updateTechnologyCuration(technology.id, {
        featured,
        orgNotes: orgNotes.trim() || null,
      })

      const existingProjectIds = technology.relatedProjects?.map((project) => project.id) ?? []
      const toAdd = linkedProjectIds.filter((projectId) => !existingProjectIds.includes(projectId))
      const toRemove = existingProjectIds.filter((projectId) => !linkedProjectIds.includes(projectId))

      for (const projectId of toAdd) {
        await learnApi.addProjectLink(technology.id, projectId)
      }
      for (const projectId of toRemove) {
        await learnApi.removeProjectLink(technology.id, projectId)
      }

      onSuccess()
    } catch (error) {
      setFormError(resolveApiError(error, LEARN_MESSAGES.curationError))
      const validationErrors = getValidationErrors(error)
      if (validationErrors?.orgNotes) {
        setFormError(validationErrors.orgNotes)
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (!technology) {
    return null
  }

  return (
    <Dialog fullWidth maxWidth="md" onClose={onClose} open={open}>
      <DialogTitle>{LEARN_MESSAGES.curationDialogTitle}</DialogTitle>
      <form noValidate onSubmit={(event) => void handleSubmit(event)}>
        <DialogContent>
          <Stack spacing={3} sx={{ pt: 1 }}>
            {formError ? <Alert severity="error">{formError}</Alert> : null}

            <Box>
              <Typography color="text.secondary" gutterBottom variant="overline">
                {LEARN_MESSAGES.curationCatalogMetadata}
              </Typography>
              <Stack spacing={1}>
                <Typography variant="h6">{technology.name}</Typography>
                <Stack direction="row" spacing={1}>
                  <TechnologyCategoryChip category={technology.category} />
                  <TechnologyDifficultyChip difficulty={technology.difficulty} />
                  <TechnologyStatusChip status={technology.status} />
                </Stack>
                <Typography>{technology.description || technology.shortName}</Typography>
                {technology.estimatedDuration ? (
                  <Typography color="text.secondary" variant="body2">
                    {LEARN_MESSAGES.curationEstimatedDuration}: {technology.estimatedDuration}
                  </Typography>
                ) : null}
                <Stack direction="row" spacing={1}>
                  {technology.tags?.map((tag) => <Chip key={tag} label={tag} size="small" variant="outlined" />)}
                </Stack>
                <Stack spacing={0.5}>
                  {technology.officialWebsite ? (
                    <Link href={technology.officialWebsite} rel="noopener noreferrer" target="_blank">
                      {LEARN_MESSAGES.curationOfficialWebsite}
                    </Link>
                  ) : null}
                  {technology.officialDocumentation ? (
                    <Link href={technology.officialDocumentation} rel="noopener noreferrer" target="_blank">
                      {LEARN_MESSAGES.curationOfficialDocumentation}
                    </Link>
                  ) : null}
                </Stack>
                <Typography color="text.secondary" variant="caption">
                  {LEARN_MESSAGES.curationCatalogSource}: {technology.catalogSource ?? '—'} ·{' '}
                  {LEARN_MESSAGES.curationCatalogVersion}: {technology.catalogVersion ?? '—'}
                </Typography>
              </Stack>
            </Box>

            <Box>
              <Typography color="text.secondary" gutterBottom variant="overline">
                {LEARN_MESSAGES.curationOrganizationControls}
              </Typography>
              <Stack spacing={2}>
                <FormControlLabel
                  control={<Switch checked={featured} onChange={(event) => setFeatured(event.target.checked)} />}
                  label={LEARN_MESSAGES.formFeatured}
                />
                <TextField
                  fullWidth
                  helperText={LEARN_MESSAGES.curationOrgNotesHelper}
                  label={LEARN_MESSAGES.curationOrgNotes}
                  minRows={3}
                  multiline
                  onChange={(event) => setOrgNotes(event.target.value)}
                  value={orgNotes}
                />
                <Autocomplete
                  getOptionLabel={(option) => option.label}
                  isOptionEqualToValue={(option, value) => option.id === value.id}
                  loading={loadingProjects}
                  multiple
                  onChange={(_event, value) => setLinkedProjectIds(value.map((item) => item.id))}
                  options={projectOptions}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      helperText={LEARN_MESSAGES.formProjectLinksHelper}
                      label={LEARN_MESSAGES.formProjectLinks}
                    />
                  )}
                  value={projectOptions.filter((option) => linkedProjectIds.includes(option.id))}
                />
              </Stack>
            </Box>
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button disabled={submitting} onClick={onClose}>
            {LEARN_MESSAGES.formCancel}
          </Button>
          <Button disabled={submitting} startIcon={submitting ? <CircularProgress size={18} /> : null} type="submit" variant="contained">
            {LEARN_MESSAGES.curationSave}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  )
}
