import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import axios from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { usersApi } from '../../api/usersApi'
import { CreateUserDialog } from './CreateUserDialog'

vi.mock('../../api/usersApi', () => ({
  usersApi: {
    create: vi.fn(),
  },
}))

function getDialog() {
  return within(screen.getByRole('dialog'))
}

describe('CreateUserDialog', () => {
  const onClose = vi.fn()
  const onSuccess = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('defaults role to EMPLOYEE', () => {
    render(<CreateUserDialog onClose={onClose} onSuccess={onSuccess} open />)

    expect(getDialog().getByRole('combobox', { name: 'Role' })).toHaveTextContent('Employee')
  })

  it('disables save until the form is valid', () => {
    render(<CreateUserDialog onClose={onClose} onSuccess={onSuccess} open />)

    expect(getDialog().getByRole('button', { name: 'Save' })).toBeDisabled()
  })

  it('creates a user with normalized email and shows success flow', async () => {
    const user = userEvent.setup()
    vi.mocked(usersApi.create).mockResolvedValue({
      active: true,
      createdAtUtc: '2026-06-12T00:00:00Z',
      email: 'jane.doe@example.com',
      employeeId: 'EMP010',
      fullName: 'Jane Doe',
      id: 'user-10',
      role: 'EMPLOYEE',
      updatedAtUtc: '2026-06-12T00:00:00Z',
    })

    render(<CreateUserDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getDialog()

    await user.type(dialog.getByRole('textbox', { name: 'Employee ID' }), 'EMP010')
    await user.type(dialog.getByRole('textbox', { name: 'Full Name' }), 'Jane Doe')
    await user.type(dialog.getByRole('textbox', { name: 'Email' }), '  Jane.Doe@Example.COM  ')
    await user.type(dialog.getByLabelText(/^Password/), 'Temp@12345')
    await user.type(dialog.getByLabelText(/^Confirm Password/), 'Temp@12345')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    await waitFor(() =>
      expect(usersApi.create).toHaveBeenCalledWith({
        employeeId: 'EMP010',
        fullName: 'Jane Doe',
        email: 'jane.doe@example.com',
        role: 'EMPLOYEE',
        password: 'Temp@12345',
      }),
    )
    expect(onSuccess).toHaveBeenCalledTimes(1)
    expect(onClose).not.toHaveBeenCalled()
  })

  it('shows server validation errors', async () => {
    const user = userEvent.setup()
    const error = new axios.AxiosError(
      'Request failed',
      'ERR_BAD_REQUEST',
      undefined,
      undefined,
      {
        data: {
          message: 'Email already exists',
          validationErrors: {
            email: 'must be a well-formed email address',
          },
        },
        headers: {},
        status: 400,
        statusText: 'Bad Request',
        config: { headers: new axios.AxiosHeaders() },
      },
    )
    vi.mocked(usersApi.create).mockRejectedValue(error)

    render(<CreateUserDialog onClose={onClose} onSuccess={onSuccess} open />)
    const dialog = getDialog()

    await user.type(dialog.getByRole('textbox', { name: 'Employee ID' }), 'EMP010')
    await user.type(dialog.getByRole('textbox', { name: 'Full Name' }), 'Jane Doe')
    await user.type(dialog.getByRole('textbox', { name: 'Email' }), 'jane.doe@example.com')
    await user.type(dialog.getByLabelText(/^Password/), 'Temp@12345')
    await user.type(dialog.getByLabelText(/^Confirm Password/), 'Temp@12345')
    await user.click(dialog.getByRole('button', { name: 'Save' }))

    expect(await dialog.findByText('Email already exists')).toBeInTheDocument()
    expect(dialog.getByText('must be a well-formed email address')).toBeInTheDocument()
  })
})
