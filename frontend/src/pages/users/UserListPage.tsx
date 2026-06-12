import { useCallback, useEffect, useMemo, useState } from 'react'
import { Alert, Box, Button, Snackbar } from '@mui/material'
import { useSearchParams } from 'react-router-dom'
import { usersApi } from '../../api/usersApi'
import { useAuth } from '../../auth/useAuth'
import { PageHeader } from '../../components/common/PageHeader'
import { TablePaginationBar } from '../../components/common/TablePaginationBar'
import { CreateUserDialog } from '../../components/users/CreateUserDialog'
import { EditUserDialog } from '../../components/users/EditUserDialog'
import { UserFilters } from '../../components/users/UserFilters'
import { UserListToolbar } from '../../components/users/UserListToolbar'
import { UserTable } from '../../components/users/UserTable'
import type { PageResponse } from '../../types/api'
import type { UserListQuery, UserSummary } from '../../types/users'
import { DEFAULT_USER_LIST_QUERY } from '../../types/users'
import {
  buildUserListSearchParams,
  parseUserListQuery,
  toApiParams,
  toggleSort,
} from './userListParams'

const EMPTY_PAGE: PageResponse<UserSummary> = {
  content: [],
  page: 0,
  size: DEFAULT_USER_LIST_QUERY.size,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
  sort: [{ property: 'employeeId', direction: 'ASC' }],
}

export function UserListPage() {
  const { user: currentUser } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()
  const appliedQuery = useMemo(() => parseUserListQuery(searchParams), [searchParams])
  const [draftQuery, setDraftQuery] = useState<UserListQuery>(appliedQuery)
  const [pageData, setPageData] = useState<PageResponse<UserSummary>>(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [editingUser, setEditingUser] = useState<UserSummary | null>(null)
  const [snackbar, setSnackbar] = useState<{ message: string; severity: 'success' | 'error' } | null>(
    null,
  )
  const [refreshToken, setRefreshToken] = useState(0)

  useEffect(() => {
    setDraftQuery(appliedQuery)
  }, [appliedQuery])

  const updateQuery = useCallback(
    (nextQuery: UserListQuery) => {
      setSearchParams(buildUserListSearchParams(nextQuery), { replace: true })
    },
    [setSearchParams],
  )

  const refreshUsers = useCallback(() => {
    setRefreshToken((current) => current + 1)
  }, [])

  useEffect(() => {
    let mounted = true

    async function loadUsers() {
      setLoading(true)
      setError(null)
      try {
        const response = await usersApi.list(toApiParams(appliedQuery))
        if (mounted) {
          setPageData(response)
        }
      } catch {
        if (mounted) {
          setError('Unable to load users. Please refresh or try again later.')
          setPageData(EMPTY_PAGE)
        }
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }

    loadUsers()
    return () => {
      mounted = false
    }
  }, [appliedQuery, refreshToken])

  const showMustChangePasswordColumn = useMemo(
    () => pageData.content.some((user) => user.mustChangePassword !== undefined),
    [pageData.content],
  )

  function handleApplyFilters() {
    updateQuery({ ...draftQuery, page: 0 })
  }

  function handleClearFilters() {
    updateQuery({ ...DEFAULT_USER_LIST_QUERY })
  }

  function handleSort(property: string) {
    updateQuery({
      ...appliedQuery,
      sort: toggleSort(appliedQuery.sort, property),
      page: 0,
    })
  }

  function handlePageChange(page: number) {
    updateQuery({ ...appliedQuery, page })
  }

  function handleSizeChange(size: number) {
    updateQuery({ ...appliedQuery, size, page: 0 })
  }

  function handleCreateSuccess() {
    setSnackbar({ message: 'User created successfully.', severity: 'success' })
    refreshUsers()
  }

  function handleEditSuccess() {
    setSnackbar({ message: 'User updated successfully.', severity: 'success' })
    refreshUsers()
  }

  const hasActiveFilters =
    Boolean(appliedQuery.employeeId) ||
    Boolean(appliedQuery.fullName) ||
    Boolean(appliedQuery.email) ||
    Boolean(appliedQuery.role) ||
    Boolean(appliedQuery.active)

  return (
    <Box>
      <PageHeader
        description="Search, filter, and manage employee accounts."
        title="User Management"
      />
      <UserListToolbar onCreateUser={() => setCreateOpen(true)} />
      {error ? (
        <Alert
          action={
            <Button color="inherit" onClick={refreshUsers} size="small">
              Retry
            </Button>
          }
          severity="error"
          sx={{ mb: 3 }}
        >
          {error}
        </Alert>
      ) : null}
      <UserFilters
        draft={draftQuery}
        onApply={handleApplyFilters}
        onClear={handleClearFilters}
        onDraftChange={setDraftQuery}
      />
      <UserTable
        hasActiveFilters={hasActiveFilters}
        loading={loading}
        onEdit={setEditingUser}
        onSort={handleSort}
        showMustChangePasswordColumn={showMustChangePasswordColumn}
        sort={appliedQuery.sort}
        users={pageData.content}
      />
      {!loading && !error && pageData.totalElements > 0 ? (
        <TablePaginationBar
          onPageChange={handlePageChange}
          onSizeChange={handleSizeChange}
          page={pageData.page}
          size={pageData.size}
          totalElements={pageData.totalElements}
        />
      ) : null}
      <CreateUserDialog
        onClose={() => setCreateOpen(false)}
        onSuccess={handleCreateSuccess}
        open={createOpen}
      />
      <EditUserDialog
        currentUserId={currentUser?.id}
        onClose={() => setEditingUser(null)}
        onSuccess={handleEditSuccess}
        open={Boolean(editingUser)}
        user={editingUser}
      />
      <Snackbar
        anchorOrigin={{ horizontal: 'center', vertical: 'bottom' }}
        autoHideDuration={5000}
        onClose={() => setSnackbar(null)}
        open={Boolean(snackbar)}
      >
        {snackbar ? (
          <Alert onClose={() => setSnackbar(null)} severity={snackbar.severity} sx={{ width: '100%' }}>
            {snackbar.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </Box>
  )
}
