import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { initiativesApi } from '../../api/initiativesApi'
import type { InitiativeSummary } from '../../api/initiativesApi'
import { loadAllMySubmissions } from '../../api/loadAllMySubmissions'
import { submissionsApi } from '../../api/submissionsApi'
import { PageHeader } from '../../components/common/PageHeader'
import type { SubmitCertificateValues } from '../../components/submissions/SubmitCertificateForm'
import { SubmitCertificateForm } from '../../components/submissions/SubmitCertificateForm'
import { SubmitCertificateDiagnosticsPanel } from '../../components/submissions/SubmitCertificateDiagnosticsPanel'
import {
  buildSubmitCertificateDiagnostics,
  logSubmitCertificateDiagnostics,
  SUBMIT_CERTIFICATE_DIAGNOSTICS_FLAG,
} from '../../components/submissions/submitCertificateDiagnostics'
import {
  extractSubmittedInitiativeIds,
  filterAvailableInitiatives,
} from '../../components/submissions/submissionInitiativeFilter'
import { SUBMISSION_MESSAGES } from '../../components/submissions/submissionMessages'
import type { CertificateSubmission } from '../../types/submissions'
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

export const SUBMIT_CERTIFICATE_SUBMISSION_PARAMS = {
  page: 0,
  size: 100,
  sort: 'submittedAtUtc,desc',
}

function parseInitiatives(content: InitiativeSummary[] | undefined | null) {
  if (!Array.isArray(content)) {
    return []
  }

  return content.filter(
    (initiative): initiative is InitiativeSummary =>
      Boolean(initiative?.id) && Boolean(initiative?.title),
  )
}

export function SubmitCertificatePage() {
  const navigate = useNavigate()
  const [initiatives, setInitiatives] = useState<InitiativeSummary[]>([])
  const [mySubmissions, setMySubmissions] = useState<CertificateSubmission[]>([])
  const [loadingInitiatives, setLoadingInitiatives] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [diagnostics, setDiagnostics] = useState<ReturnType<typeof buildSubmitCertificateDiagnostics> | null>(
    null,
  )

  useEffect(() => {
    let mounted = true

    async function loadPageData() {
      setLoadingInitiatives(true)
      setLoadError(null)

      let rawInitiativesResponse: Awaited<ReturnType<typeof initiativesApi.list>> | { error: string }
      let loadedInitiatives: InitiativeSummary[] = []
      let rawSubmissionsResponse: CertificateSubmission[] | { error: string } = []
      let loadedSubmissions: CertificateSubmission[] = []

      try {
        const initiativePage = await initiativesApi.list(SUBMIT_CERTIFICATE_INITIATIVE_PARAMS)
        rawInitiativesResponse = initiativePage
        loadedInitiatives = parseInitiatives(initiativePage.content)
        if (!mounted) {
          return
        }
        setInitiatives(loadedInitiatives)
      } catch (error) {
        rawInitiativesResponse = {
          error: resolveApiError(error, SUBMISSION_MESSAGES.initiativesLoadError),
        }
        if (mounted) {
          setInitiatives([])
          setMySubmissions([])
          setLoadError(rawInitiativesResponse.error)
          const nextDiagnostics = buildSubmitCertificateDiagnostics({
            initiativeParams: SUBMIT_CERTIFICATE_INITIATIVE_PARAMS,
            submissionParams: SUBMIT_CERTIFICATE_SUBMISSION_PARAMS,
            rawInitiativesResponse,
            rawSubmissionsResponse,
            initiatives: [],
            submissions: [],
          })
          setDiagnostics(nextDiagnostics)
          logSubmitCertificateDiagnostics(nextDiagnostics)
          setLoadingInitiatives(false)
        }
        return
      }

      try {
        loadedSubmissions = await loadAllMySubmissions()
        rawSubmissionsResponse = loadedSubmissions
        if (mounted) {
          setMySubmissions(loadedSubmissions)
        }
      } catch (error) {
        rawSubmissionsResponse = {
          error: resolveApiError(error, SUBMISSION_MESSAGES.loadError),
        }
        if (mounted) {
          setMySubmissions([])
        }
      }

      if (mounted) {
        const nextDiagnostics = buildSubmitCertificateDiagnostics({
          initiativeParams: SUBMIT_CERTIFICATE_INITIATIVE_PARAMS,
          submissionParams: SUBMIT_CERTIFICATE_SUBMISSION_PARAMS,
          rawInitiativesResponse,
          rawSubmissionsResponse,
          initiatives: loadedInitiatives,
          submissions: loadedSubmissions,
        })
        setDiagnostics(nextDiagnostics)
        logSubmitCertificateDiagnostics(nextDiagnostics)
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
      {SUBMIT_CERTIFICATE_DIAGNOSTICS_FLAG && diagnostics ? (
        <SubmitCertificateDiagnosticsPanel diagnostics={diagnostics} />
      ) : null}
      {loadingInitiatives ? (
        <Box aria-label="Loading initiatives" sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress />
        </Box>
      ) : (
        <SubmitCertificateForm
          emptyMessage={emptyMessage}
          infoMessage={infoMessage}
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
