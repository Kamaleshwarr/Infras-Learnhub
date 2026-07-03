import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import BlockOutlinedIcon from '@mui/icons-material/BlockOutlined'
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined'
import LinkOutlinedIcon from '@mui/icons-material/LinkOutlined'
import RestoreOutlinedIcon from '@mui/icons-material/RestoreOutlined'
import StarBorderOutlinedIcon from '@mui/icons-material/StarBorderOutlined'
import StarOutlinedIcon from '@mui/icons-material/StarOutlined'
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
  IconButton,
  MenuItem,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
  alpha,
} from '@mui/material'
import { learnApi } from '../../api/learnApi'
import type { ManagedResource } from '../../types/resourceOverride'
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

const ROW_MIN_HEIGHT = 72

type StatusChip = {
  label: string
  color: 'default' | 'primary' | 'warning' | 'error' | 'success' | 'info'
}

function getStatusChips(resource: ManagedResource): StatusChip[] {
  const chips: StatusChip[] = []

  if (resource.overrideStatus === 'ORGANIZATION' || (!resource.catalog && resource.effective)) {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusOrganization, color: 'primary' })
    return chips
  }

  if (resource.overrideStatus === 'DISABLED') {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusDisabled, color: 'error' })
    return chips
  }

  if (resource.overrideStatus === 'URL_OVERRIDE') {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusUrlOverride, color: 'info' })
  } else if (resource.overrideStatus === 'PREFERRED') {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusPreferred, color: 'success' })
  } else if (resource.overrideStatus === 'INACTIVE') {
    chips.push({ label: 'Inactive', color: 'default' })
  } else {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusCatalogDefault, color: 'default' })
  }

  if (resource.override?.preferred && resource.overrideStatus !== 'PREFERRED') {
    chips.push({ label: LEARN_MESSAGES.roadmapStatusPreferred, color: 'success' })
  }

  return chips
}

function urlsMatch(resource: ManagedResource) {
  if (!resource.catalog || !resource.effective) {
    return false
  }
  return resource.catalog.url === resource.effective.url
}

function isHiddenFromEmployees(resource: ManagedResource) {
  return resource.overrideStatus === 'DISABLED' || (resource.catalog != null && resource.effective == null)
}

function ResourceCell({ resource }: { resource: ManagedResource }) {
  const hidden = isHiddenFromEmployees(resource)
  const organizationOnly = !resource.catalog && resource.effective

  if (organizationOnly) {
    return (
      <Stack spacing={0.5}>
        <Typography sx={{ fontWeight: 600 }} variant="body2">
          {resource.effective!.title}
        </Typography>
        <Typography
          color="text.secondary"
          sx={{ fontFamily: 'monospace', fontSize: '0.75rem', wordBreak: 'break-all' }}
          variant="caption"
        >
          {resource.effective!.url}
        </Typography>
      </Stack>
    )
  }

  if (!resource.catalog) {
    return (
      <Typography color="text.secondary" variant="body2">
        —
      </Typography>
    )
  }

  const showEmployeeUrl = resource.effective != null && !urlsMatch(resource)

  return (
    <Stack spacing={0.75}>
      <Typography
        color={hidden ? 'text.disabled' : 'text.primary'}
        sx={{ fontWeight: 600, textDecoration: hidden ? 'line-through' : 'none' }}
        variant="body2"
      >
        {resource.catalog.title}
      </Typography>

      <Box>
        {showEmployeeUrl ? (
          <Stack spacing={0.35}>
            <Typography color="text.secondary" variant="caption">
              {LEARN_MESSAGES.roadmapCatalogUrl}
            </Typography>
            <Typography
              color={hidden ? 'text.disabled' : 'text.secondary'}
              sx={{ fontFamily: 'monospace', fontSize: '0.75rem', wordBreak: 'break-all' }}
              variant="caption"
            >
              {resource.catalog.url}
            </Typography>
            <Typography color="text.secondary" sx={{ mt: 0.5 }} variant="caption">
              {LEARN_MESSAGES.roadmapEmployeeUrl}
            </Typography>
            <Typography
              color="info.main"
              sx={{ fontFamily: 'monospace', fontSize: '0.75rem', wordBreak: 'break-all' }}
              variant="caption"
            >
              {resource.effective!.url}
            </Typography>
          </Stack>
        ) : (
          <Typography
            color={hidden ? 'text.disabled' : 'text.secondary'}
            sx={{ fontFamily: 'monospace', fontSize: '0.75rem', wordBreak: 'break-all' }}
            variant="caption"
          >
            {resource.catalog.url}
          </Typography>
        )}
      </Box>

      {hidden ? (
        <Chip
          color="default"
          label={LEARN_MESSAGES.roadmapOverrideHidden}
          size="small"
          sx={{ alignSelf: 'flex-start', height: 22 }}
          variant="outlined"
        />
      ) : null}
    </Stack>
  )
}

function StatusCell({ resource }: { resource: ManagedResource }) {
  const chips = getStatusChips(resource)

  return (
    <Stack direction="row" spacing={0.75} sx={{ flexWrap: 'wrap', gap: 0.75 }}>
      {chips.map((chip) => (
        <Chip
          color={chip.color}
          key={chip.label}
          label={chip.label}
          size="small"
          sx={{ height: 24 }}
          variant="outlined"
        />
      ))}
    </Stack>
  )
}

function ActionsCell({
  resource,
  submitting,
  onReplaceUrl,
  onDisable,
  onRestore,
  onTogglePreferred,
  onRemoveOrganization,
}: {
  resource: ManagedResource
  submitting: boolean
  onReplaceUrl: (resource: ManagedResource) => void
  onDisable: (resource: ManagedResource) => void
  onRestore: (resource: ManagedResource) => void
  onTogglePreferred: (resource: ManagedResource) => void
  onRemoveOrganization: (resource: ManagedResource) => void
}) {
  const organizationOnly = !resource.catalog

  if (organizationOnly) {
    return (
      <Box sx={{ alignItems: 'center', display: 'flex', justifyContent: 'flex-end', minHeight: 40 }}>
        <Tooltip title={LEARN_MESSAGES.roadmapRemoveOrganizationResource}>
          <span>
            <IconButton
              aria-label={LEARN_MESSAGES.roadmapRemoveOrganizationResource}
              color="error"
              disabled={submitting}
              onClick={() => onRemoveOrganization(resource)}
              size="small"
            >
              <DeleteOutlineOutlinedIcon fontSize="small" />
            </IconButton>
          </span>
        </Tooltip>
      </Box>
    )
  }

  const hidden = isHiddenFromEmployees(resource)
  const hasOverride = resource.override != null
  const isPreferred = resource.override?.preferred ?? false

  return (
    <Box sx={{ alignItems: 'center', display: 'flex', gap: 0.5, justifyContent: 'flex-end', minHeight: 40 }}>
      <Tooltip title={LEARN_MESSAGES.roadmapReplaceUrl}>
        <span>
          <IconButton
            aria-label={LEARN_MESSAGES.roadmapReplaceUrl}
            disabled={submitting || hidden}
            onClick={() => onReplaceUrl(resource)}
            size="small"
          >
            <LinkOutlinedIcon fontSize="small" />
          </IconButton>
        </span>
      </Tooltip>

      {!hidden ? (
        <Tooltip title={LEARN_MESSAGES.roadmapDisableResource}>
          <span>
            <IconButton
              aria-label={LEARN_MESSAGES.roadmapDisableResource}
              disabled={submitting}
              onClick={() => onDisable(resource)}
              size="small"
            >
              <BlockOutlinedIcon fontSize="small" />
            </IconButton>
          </span>
        </Tooltip>
      ) : null}

      {hasOverride ? (
        <>
          <Tooltip title={LEARN_MESSAGES.roadmapRestoreDefault}>
            <span>
              <IconButton
                aria-label={LEARN_MESSAGES.roadmapRestoreDefault}
                disabled={submitting}
                onClick={() => onRestore(resource)}
                size="small"
              >
                <RestoreOutlinedIcon fontSize="small" />
              </IconButton>
            </span>
          </Tooltip>

          <Tooltip title={isPreferred ? LEARN_MESSAGES.roadmapUnmarkPreferred : LEARN_MESSAGES.roadmapMarkPreferred}>
            <span>
              <IconButton
                aria-label={isPreferred ? LEARN_MESSAGES.roadmapUnmarkPreferred : LEARN_MESSAGES.roadmapMarkPreferred}
                color={isPreferred ? 'warning' : 'default'}
                disabled={submitting || hidden}
                onClick={() => onTogglePreferred(resource)}
                size="small"
              >
                {isPreferred ? <StarOutlinedIcon fontSize="small" /> : <StarBorderOutlinedIcon fontSize="small" />}
              </IconButton>
            </span>
          </Tooltip>
        </>
      ) : null}
    </Box>
  )
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
      <DialogTitle sx={{ pb: 1 }}>
        <Typography component="span" variant="h6">
          {LEARN_MESSAGES.roadmapManageResourcesTitle}
        </Typography>
        <Typography color="text.secondary" sx={{ display: 'block', mt: 0.5 }} variant="body2">
          {stageTitle}
        </Typography>
      </DialogTitle>

      <DialogContent dividers sx={{ pt: 2 }}>
        <Stack spacing={2.5}>
          {mode === 'list' ? (
            <Typography color="text.secondary" variant="body2">
              {LEARN_MESSAGES.roadmapManageResourcesDescription}
            </Typography>
          ) : null}

          {error ? <Alert severity="error">{error}</Alert> : null}

          {mode === 'replace-url' && selectedResource?.catalog ? (
            <Box component="form" id="replace-url-form" onSubmit={(event) => void handleReplaceUrl(event)}>
              <Stack spacing={2.5}>
                <Box>
                  <Typography color="text.secondary" gutterBottom variant="overline">
                    {LEARN_MESSAGES.roadmapReplaceUrl}
                  </Typography>
                  <Typography sx={{ fontWeight: 600 }} variant="subtitle1">
                    {selectedResource.catalog.title}
                  </Typography>
                </Box>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideUrl}
                  onChange={(event) => setOverrideUrl(event.target.value)}
                  required
                  size="small"
                  value={overrideUrl}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideReason}
                  multiline
                  onChange={(event) => setReason(event.target.value)}
                  rows={2}
                  size="small"
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
              <Stack spacing={2.5}>
                <Typography color="text.secondary" variant="overline">
                  {LEARN_MESSAGES.roadmapAddOrganizationResource}
                </Typography>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceTitle}
                  onChange={(event) => setOrgTitle(event.target.value)}
                  required
                  size="small"
                  value={orgTitle}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceSlug}
                  onChange={(event) => setOrgSlug(event.target.value)}
                  required
                  size="small"
                  value={orgSlug}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideUrl}
                  onChange={(event) => setOverrideUrl(event.target.value)}
                  required
                  size="small"
                  value={overrideUrl}
                />
                <Stack direction={{ sm: 'row' }} spacing={2}>
                  <TextField
                    fullWidth
                    label={LEARN_MESSAGES.roadmapOrganizationResourceType}
                    onChange={(event) => setOrgType(event.target.value as RoadmapResourceType)}
                    select
                    size="small"
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
                    label={LEARN_MESSAGES.roadmapOrganizationResourceCost}
                    onChange={(event) => setOrgCost(event.target.value as RoadmapResourceCost)}
                    select
                    size="small"
                    value={orgCost}
                  >
                    {RESOURCE_COST_OPTIONS.map((option) => (
                      <MenuItem key={option.value} value={option.value}>
                        {option.label}
                      </MenuItem>
                    ))}
                  </TextField>
                </Stack>
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOrganizationResourceProvider}
                  onChange={(event) => setOrgProvider(event.target.value)}
                  size="small"
                  value={orgProvider}
                />
                <TextField
                  fullWidth
                  label={LEARN_MESSAGES.roadmapOverrideReason}
                  multiline
                  onChange={(event) => setReason(event.target.value)}
                  rows={2}
                  size="small"
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
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 5 }}>
                  <CircularProgress size={28} />
                </Box>
              ) : (
                <TableContainer
                  sx={{
                    border: 1,
                    borderColor: 'divider',
                    borderRadius: 1,
                    overflowX: 'auto',
                  }}
                >
                  <Table size="small" sx={{ minWidth: 640 }}>
                    <TableHead>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 600, width: '46%' }}>{LEARN_MESSAGES.roadmapResourceColumn}</TableCell>
                        <TableCell sx={{ fontWeight: 600, width: '24%' }}>{LEARN_MESSAGES.roadmapStatusColumn}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 600, width: '30%' }}>
                          {LEARN_MESSAGES.roadmapActionsColumn}
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {resources.map((resource) => {
                        const key = resource.catalog?.slug ?? resource.effective?.slug ?? resource.override?.id ?? 'resource'
                        const hidden = isHiddenFromEmployees(resource)

                        return (
                          <TableRow
                            key={key}
                            sx={(theme) => ({
                              '&:last-child td': { borderBottom: 0 },
                              bgcolor: hidden ? alpha(theme.palette.action.disabled, 0.04) : 'transparent',
                              minHeight: ROW_MIN_HEIGHT,
                            })}
                          >
                            <TableCell sx={{ py: 1.75, verticalAlign: 'top' }}>
                              <ResourceCell resource={resource} />
                            </TableCell>
                            <TableCell sx={{ py: 1.75, verticalAlign: 'middle' }}>
                              <StatusCell resource={resource} />
                            </TableCell>
                            <TableCell align="right" sx={{ py: 1.25, verticalAlign: 'middle' }}>
                              <ActionsCell
                                onDisable={(item) => void handleDisable(item)}
                                onRemoveOrganization={(item) => void handleDeleteOrganizationResource(item)}
                                onReplaceUrl={openReplaceUrl}
                                onRestore={(item) => void handleRestore(item)}
                                onTogglePreferred={(item) => void handleTogglePreferred(item)}
                                resource={resource}
                                submitting={submitting}
                              />
                            </TableCell>
                          </TableRow>
                        )
                      })}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </>
          ) : null}
        </Stack>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        {mode === 'list' ? (
          <>
            <Button onClick={() => setMode('add-org')} variant="outlined">
              {LEARN_MESSAGES.roadmapAddOrganizationResource}
            </Button>
            <Box sx={{ flex: 1 }} />
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
