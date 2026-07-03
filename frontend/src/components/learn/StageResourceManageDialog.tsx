import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  MenuItem,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material'
import { learnApi } from '../../api/learnApi'
import type { ManagedResource, ResourceOverrideStatus } from '../../types/resourceOverride'
import type { RoadmapResourceCost, RoadmapResourceType } from '../../types/roadmap'
import { resolveApiError } from '../../utils/apiErrors'
import { LEARN_MESSAGES } from './learnMessages'

interface StageResourceManageDialogProps {
  open: boolean
  technologyId: string
  stageSlug: string
  stageTitle: string
  onClose: () => void
  onSuccess: () => void
}

type DialogMode = 'list' | 'replace-url' | 'add-org'

const RESOURCE_TYPE_OPTIONS: Array<{ value: RoadmapResourceType; label: string }> = [
  { value: 'OFFICIAL_DOCUMENTATION', label: 'Official documentation' },
  { value: 'OFFICIAL_TUTORIAL', label: 'Official tutorial' },
  { value: 'OPEN_EDUCATIONAL_RESOURCE', label: 'Open educational resource' },
  { value: 'VIDEO', label: 'Video' },
  { value: 'ARTICLE', label: 'Article' },
  { value: 'GITHUB', label: 'GitHub' },
  { value: 'INTERACTIVE_TUTORIAL', label: 'Interactive tutorial' },
  { value: 'PRACTICE_LAB', label: 'Practice lab' },
  { value: 'OTHER', label: 'Other' },
]

const RESOURCE_COST_OPTIONS: Array<{ value: RoadmapResourceCost; label: string }> = [
  { value: 'FREE', label: 'Free' },
  { value: 'PAID', label: 'Paid' },
  { value: 'FREEMIUM', label: 'Freemium' },
]

function statusLabel(status: ResourceOverrideStatus) {
  switch (status) {
    case 'URL_OVERRIDE':
      return 'URL override'
    case 'DISABLED':
      return 'Disabled'
    case 'PREFERRED':
      return 'Preferred'
    case 'ORGANIZATION':
      return 'Organization'
    case 'INACTIVE':
      return 'Inactive'
    default:
      return 'Catalog default'
  }
}

function statusColor(status: ResourceOverrideStatus): 'default' | 'primary' | 'warning' | 'error' | 'success' | 'info' {
  switch (status) {
    case 'URL_OVERRIDE':
      return 'info'
    case 'DISABLED':
      return 'error'
    case 'PREFERRED':
      return 'success'
    case 'ORGANIZATION':
      return 'primary'
    default:
      return 'default'
  }
}

export function StageResourceManageDialog({
  open,
  technologyId,
  stageSlug,
  stageTitle,
  onClose,
  onSuccess,
}: StageResourceManageDialogProps) {
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [resources, setResources] = useState<ManagedResource[]>([])
  const [error, setError] = useState<string | null>(null)
  const [mode, setMode] = useState<DialogMode>('list')
  const [selectedResource, setSelectedResource] = useState<ManagedResource | null>(null)
  const [overrideUrl, setOverrideUrl] = useState('')
  const [reason, setReason] = useState('')
  const [preferred, setPreferred] = useState(false)
  const [orgTitle, setOrgTitle] = useState('')
  const [orgSlug, setOrgSlug] = useState('')
  const [orgType, setOrgType] = useState<RoadmapResourceType>('OTHER')
  const [orgProvider, setOrgProvider] = useState('')
  const [orgCost, setOrgCost] = useState<RoadmapResourceCost>('FREE')

  useEffect(() => {
    if (!open) {
      return
    }

    setMode('list')
    setSelectedResource(null)
    setError(null)
    void loadResources()
  }, [open, technologyId, stageSlug])

  async function loadResources() {
    setLoading(true)
    setError(null)
    try {
      const response = await learnApi.getStageResources(technologyId, stageSlug)
      setResources(response.resources)
    } catch (loadError) {
      setError(resolveApiError(loadError, LEARN_MESSAGES.roadmapOverrideError))
      setResources([])
    } finally {
      setLoading(false)
    }
  }

  function resetForm() {
    setOverrideUrl('')
    setReason('')
    setPreferred(false)
    setOrgTitle('')
    setOrgSlug('')
    setOrgType('OTHER')
    setOrgProvider('')
    setOrgCost('FREE')
    setSelectedResource(null)
    setMode('list')
    setError(null)
  }

  function openReplaceUrl(resource: ManagedResource) {
    setSelectedResource(resource)
    setOverrideUrl(resource.override?.overrideUrl ?? resource.catalog?.url ?? '')
    setReason(resource.override?.reason ?? '')
    setPreferred(resource.override?.preferred ?? false)
    setMode('replace-url')
  }

  async function handleReplaceUrl(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selectedResource?.catalog) {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      if (selectedResource.override) {
        await learnApi.updateResourceOverride(technologyId, selectedResource.override.id, {
          disabled: false,
          overrideUrl,
          preferred,
          reason: reason || null,
          enabled: true,
        })
      } else {
        await learnApi.createResourceOverride(technologyId, {
          stageSlug,
          catalogResourceSlug: selectedResource.catalog.slug,
          resourceSlug: selectedResource.catalog.slug,
          resourceKind: 'LEARNING',
          disabled: false,
          overrideUrl,
          preferred,
          enabled: true,
          reason: reason || null,
        })
      }
      resetForm()
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDisable(resource: ManagedResource) {
    if (!resource.catalog) {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      if (resource.override) {
        await learnApi.updateResourceOverride(technologyId, resource.override.id, {
          disabled: true,
          enabled: true,
        })
      } else {
        await learnApi.createResourceOverride(technologyId, {
          stageSlug,
          catalogResourceSlug: resource.catalog.slug,
          resourceSlug: resource.catalog.slug,
          resourceKind: 'LEARNING',
          disabled: true,
          enabled: true,
        })
      }
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleRestore(resource: ManagedResource) {
    if (!resource.catalog) {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      await learnApi.restoreResourceDefault(technologyId, stageSlug, resource.catalog.slug)
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleTogglePreferred(resource: ManagedResource) {
    if (!resource.override) {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      await learnApi.updateResourceOverride(technologyId, resource.override.id, {
        preferred: !resource.override.preferred,
      })
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDeleteOrganizationResource(resource: ManagedResource) {
    if (!resource.override) {
      return
    }

    setSubmitting(true)
    setError(null)
    try {
      await learnApi.deleteResourceOverride(technologyId, resource.override.id)
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  async function handleAddOrganizationResource(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)
    try {
      await learnApi.createResourceOverride(technologyId, {
        stageSlug,
        resourceSlug: orgSlug.trim(),
        resourceKind: 'LEARNING',
        overrideUrl: overrideUrl.trim(),
        title: orgTitle.trim(),
        resourceType: orgType,
        provider: orgProvider.trim() || null,
        freePaid: orgCost,
        preferred,
        enabled: true,
        reason: reason || null,
      })
      resetForm()
      await loadResources()
      onSuccess()
    } catch (submitError) {
      setError(resolveApiError(submitError, LEARN_MESSAGES.roadmapOverrideError))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog fullWidth maxWidth="lg" onClose={onClose} open={open}>
      <DialogTitle>
        {LEARN_MESSAGES.roadmapManageResourcesTitle}: {stageTitle}
      </DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 0.5 }}>
          <Typography color="text.secondary" variant="body2">
            {LEARN_MESSAGES.roadmapManageResourcesDescription}
          </Typography>

          {error ? <Alert severity="error">{error}</Alert> : null}

          {mode === 'replace-url' && selectedResource?.catalog ? (
            <Box component="form" id="replace-url-form" onSubmit={(event) => void handleReplaceUrl(event)}>
              <Stack spacing={2}>
                <Typography variant="subtitle2">{selectedResource.catalog.title}</Typography>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideUrl}
                  onChange={(event) => setOverrideUrl(event.target.value)}
                  required
                  value={overrideUrl}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideReason}
                  multiline
                  onChange={(event) => setReason(event.target.value)}
                  rows={2}
                  value={reason}
                />
                <FormControlLabel
                  control={<Switch checked={preferred} onChange={(event) => setPreferred(event.target.checked)} />}
                  label={LEARN_MESSAGES.roadmapPreferredResource}
                />
              </Stack>
            </Box>
          ) : null}

          {mode === 'add-org' ? (
            <Box component="form" id="add-org-form" onSubmit={(event) => void handleAddOrganizationResource(event)}>
              <Stack spacing={2}>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceTitle}
                  onChange={(event) => setOrgTitle(event.target.value)}
                  required
                  value={orgTitle}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceSlug}
                  onChange={(event) => setOrgSlug(event.target.value)}
                  required
                  value={orgSlug}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideUrl}
                  onChange={(event) => setOverrideUrl(event.target.value)}
                  required
                  value={overrideUrl}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceType}
                  onChange={(event) => setOrgType(event.target.value as RoadmapResourceType)}
                  select
                  value={orgType}
                >
                  {RESOURCE_TYPE_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceProvider}
                  onChange={(event) => setOrgProvider(event.target.value)}
                  value={orgProvider}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceCost}
                  onChange={(event) => setOrgCost(event.target.value as RoadmapResourceCost)}
                  select
                  value={orgCost}
                >
                  {RESOURCE_COST_OPTIONS.map((option) => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))}
                </TextField>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideReason}
                  multiline
                  onChange={(event) => setReason(event.target.value)}
                  rows={2}
                  value={reason}
                />
                <FormControlLabel
                  control={<Switch checked={preferred} onChange={(event) => setPreferred(event.target.checked)} />}
                  label={LEARN_MESSAGES.roadmapPreferredResource}
                />
              </Stack>
            </Box>
          ) : null}

          {mode === 'list' ? (
            <>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
                  <CircularProgress size={28} />
                </Box>
              ) : (
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>{LEARN_MESSAGES.roadmapCatalogResource}</TableCell>
                      <TableCell>{LEARN_MESSAGES.roadmapEffectiveResource}</TableCell>
                      <TableCell>{LEARN_MESSAGES.roadmapOverrideStatus}</TableCell>
                      <TableCell align="right">Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {resources.map((resource) => {
                      const key = resource.catalog?.slug ?? resource.effective?.slug ?? resource.override?.id ?? 'resource'
                      return (
                        <TableRow key={key}>
                          <TableCell>
                            {resource.catalog ? (
                              <Stack spacing={0.25}>
                                <Typography variant="body2">{resource.catalog.title}</Typography>
                                <Typography color="text.secondary" variant="caption">
                                  {resource.catalog.url}
                                </Typography>
                              </Stack>
                            ) : (
                              <Typography color="text.secondary" variant="body2">
                                —
                              </Typography>
                            )}
                          </TableCell>
                          <TableCell>
                            {resource.effective ? (
                              <Stack spacing={0.25}>
                                <Typography variant="body2">{resource.effective.title}</Typography>
                                <Typography color="text.secondary" variant="caption">
                                  {resource.effective.url}
                                </Typography>
                              </Stack>
                            ) : (
                              <Typography color="text.secondary" variant="body2">
                                {LEARN_MESSAGES.roadmapOverrideHidden}
                              </Typography>
                            )}
                          </TableCell>
                          <TableCell>
                            <Chip
                              color={statusColor(resource.overrideStatus)}
                              label={statusLabel(resource.overrideStatus)}
                              size="small"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell align="right">
                            <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                              {resource.catalog ? (
                                <>
                                  <Button disabled={submitting} onClick={() => openReplaceUrl(resource)} size="small">
                                    {LEARN_MESSAGES.roadmapReplaceUrl}
                                  </Button>
                                  <Button disabled={submitting} onClick={() => void handleDisable(resource)} size="small">
                                    {LEARN_MESSAGES.roadmapDisableResource}
                                  </Button>
                                  {resource.override ? (
                                    <>
                                      <Button disabled={submitting} onClick={() => void handleRestore(resource)} size="small">
                                        {LEARN_MESSAGES.roadmapRestoreDefault}
                                      </Button>
                                      <Button
                                        disabled={submitting}
                                        onClick={() => void handleTogglePreferred(resource)}
                                        size="small"
                                      >
                                        {resource.override.preferred ? 'Unmark preferred' : 'Mark preferred'}
                                      </Button>
                                    </>
                                  ) : null}
                                </>
                              ) : (
                                <Button
                                  color="error"
                                  disabled={submitting}
                                  onClick={() => void handleDeleteOrganizationResource(resource)}
                                  size="small"
                                >
                                  Remove
                                </Button>
                              )}
                            </Stack>
                          </TableCell>
                        </TableRow>
                      )
                    })}
                  </TableBody>
                </Table>
              )}
            </>
          ) : null}
        </Stack>
      </DialogContent>
      <DialogActions>
        {mode === 'list' ? (
          <>
            <Button onClick={() => setMode('add-org')} variant="outlined">
              {LEARN_MESSAGES.roadmapAddOrganizationResource}
            </Button>
            <Button onClick={onClose}>{LEARN_MESSAGES.formCancel}</Button>
          </>
        ) : (
          <>
            <Button disabled={submitting} onClick={resetForm}>
              Back
            </Button>
            <Button
              disabled={submitting}
              form={mode === 'replace-url' ? 'replace-url-form' : 'add-org-form'}
              type="submit"
              variant="contained"
            >
              {mode === 'replace-url' ? LEARN_MESSAGES.roadmapOverrideSave : LEARN_MESSAGES.roadmapOverrideCreate}
            </Button>
          </>
        )}
      </DialogActions>
    </Dialog>
  )
}
