import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import type { InitiativeSummary } from '../../api/initiativesApi'
import { submissionsApi } from '../../api/submissionsApi'
import { PageHeader } from '../../components/common/PageHeader'
import type { SubmitCertificateValues } from '../../components/submissions/SubmitCertificateForm'
import { SubmitCertificateForm } from '../../components/submissions/SubmitCertificateForm'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import { resolveApiError } from '../../utils/apiErrors'

export interface SubmissionRouteNotification {
  message: string
  severity: 'success' | 'error'
}

export function SubmitCertificatePage() {
  const navigate = useNavigate()
  const [initiatives, setInitiatives] = useState<InitiativeSummary[]>([])
  const [submittedInitiativeIds, setSubmittedInitiativeIds] = useState<Set<string>>(new Set())
  const [loadingInitiatives, setLoadingInitiatives] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    let mounted = true

    async function loadInitiatives() {
      setLoadingInitiatives(true)
      setLoadError(null)

      try {
        const [initiativePage, submissionPage] = await Promise.all([
          initiativesApi.list({ page: 0, size: 100, sort: 'title,asc' }),
          submissionsApi.listMine({ page: 0, size: 100 }),
        ])

        if (!mounted) {
          return
        }

        setInitiatives(initiativePage.content)
        setSubmittedInitiativeIds(new Set(submissionPage.content.map((submission) => submission.initiative.id)))
      } catch (error) {
        if (mounted) {
          setInitiatives([])
          setSubmittedInitiativeIds(new Set())
          setLoadError(resolveApiError(error, SUBMISSION_MESSAGES.initiativesLoadError))
        }
      } finally {
        if (mounted) {
          setLoadingInitiatives(false)
        }
      }
    }

    void loadInitiatives()

    return () => {
      mounted = false
    }
  }, [])

  const availableInitiatives = useMemo(
    () => initiatives.filter((initiative) => !submittedInitiativeIds.has(initiative.id)),
    [initiatives, submittedInitiativeIds],
  )

  const emptyMessage = useMemo(() => {
    if (loadingInitiatives || loadError) {
      return null
    }

    if (initiatives.length === 0) {
      return SUBMISSION_MESSAGES.noInitiativesAvailable
    }

    if (availableInitiatives.length === 0) {
      return SUBMISSION_MESSAGES.allInitiativesSubmitted
    }

    return null
  }, [availableInitiatives.length, initiatives.length, loadError, loadingInitiatives])

  async function handleSubmit(values: SubmitCertificateValues) {
    setSubmitting(true)

    try {
      const formData = new FormData()
      formData.append('certificateFile', values.file)
      if (values.comments) {
        formData.append('comments', values.comments)
      }

      await submissionsApi.submit(values.initiativeId, formData)
      navigate('/submissions', {
        replace: true,
        state: {
          submissionNotification: {
            message: SUBMISSION_MESSAGES.submitSuccess,
            severity: 'success',
          } satisfies SubmissionRouteNotification,
        },
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <>
      <PageHeader
        description="Upload a certificate file and comments for an active initiative."
        title="Submit Certificate"
      />
      {loadingInitiatives ? (
        <Box aria-label="Loading initiatives" sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress />
        </Box>
      ) : (
        <SubmitCertificateForm
          emptyMessage={emptyMessage}
          initiatives={availableInitiatives}
          loadError={loadError}
          loadingInitiatives={loadingInitiatives}
          onSubmit={handleSubmit}
          submitting={submitting}
        />
      )}
    </>
  )
}
