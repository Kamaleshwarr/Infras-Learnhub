import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import { loadAllMySubmissions } from '../../api/loadAllMySubmissions'
import { submissionsApi } from '../../api/submissionsApi'
import { PageHeader } from '../../components/common/PageHeader'
import type { SubmitCertificateValues } from '../../components/submissions/SubmitCertificateForm'
import { SubmitCertificateForm } from '../../components/submissions/SubmitCertificateForm'
import {
  extractSubmittedInitiativeIds,
  filterAvailableInitiatives,
  parseInitiativeSummaries,
} from '../../components/submissions/submissionInitiativeFilter'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import { resolveApiError } from '../../utils/apiErrors'

export interface SubmissionRouteNotification {
  message: string
  severity: 'success' | 'error'
}

export const SUBMIT_CERTIFICATE_INITIATIVE_PARAMS = {
  size: 100,
  status: 'ACTIVE' as const,
  sort: 'expiryDateUtc,asc',
}

export function SubmitCertificatePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const initialInitiativeId = searchParams.get('initiativeId')
  const [initiatives, setInitiatives] = useState<ReturnType<typeof parseInitiativeSummaries>>([])
  const [mySubmissions, setMySubmissions] = useState<Awaited<ReturnType<typeof loadAllMySubmissions>>>([])
  const [loadingInitiatives, setLoadingInitiatives] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    let mounted = true

    async function loadPageData() {
      setLoadingInitiatives(true)
      setLoadError(null)

      try {
        const initiativePage = await initiativesApi.list(SUBMIT_CERTIFICATE_INITIATIVE_PARAMS)
        if (!mounted) {
          return
        }
        setInitiatives(parseInitiativeSummaries(initiativePage.content))
      } catch (error) {
        if (mounted) {
          setInitiatives([])
          setMySubmissions([])
          setLoadError(resolveApiError(error, SUBMISSION_MESSAGES.initiativesLoadError))
          setLoadingInitiatives(false)
        }
        return
      }

      try {
        const loadedSubmissions = await loadAllMySubmissions()
        if (mounted) {
          setMySubmissions(loadedSubmissions)
        }
      } catch {
        if (mounted) {
          setMySubmissions([])
        }
      }

      if (mounted) {
        setLoadingInitiatives(false)
      }
    }

    void loadPageData()

    return () => {
      mounted = false
    }
  }, [])

  const submittedInitiativeIds = useMemo(
    () => extractSubmittedInitiativeIds(mySubmissions),
    [mySubmissions],
  )

  const availableInitiatives = useMemo(
    () => filterAvailableInitiatives(initiatives, mySubmissions),
    [initiatives, mySubmissions],
  )

  const emptyMessage = useMemo(() => {
    if (loadingInitiatives || loadError) {
      return null
    }

    if (initiatives.length === 0) {
      return SUBMISSION_MESSAGES.noInitiativesAvailable
    }

    return null
  }, [initiatives.length, loadError, loadingInitiatives])

  const infoMessage = useMemo(() => {
    if (loadingInitiatives || loadError || initiatives.length === 0) {
      return null
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
          infoMessage={infoMessage}
          initialInitiativeId={initialInitiativeId}
          initiatives={initiatives}
          loadError={loadError}
          loadingInitiatives={loadingInitiatives}
          onSubmit={handleSubmit}
          submittedInitiativeIds={submittedInitiativeIds}
          submitting={submitting}
        />
      )}
    </>
  )
}
