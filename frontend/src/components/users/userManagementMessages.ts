export const USER_MANAGEMENT_MESSAGES = {
  createSuccess: 'User created successfully.',
  updateSuccess: 'User updated successfully.',
  activateSuccess: 'User activated successfully.',
  deactivateSuccess: 'User deactivated successfully.',
  resetPasswordSuccess:
    'Password reset successfully. User will be required to change their password at next sign-in.',
  importSuccess: (count: number) =>
    `${count} user${count === 1 ? '' : 's'} imported successfully.`,
  importNoRows: 'No users were imported.',
  templateDownloadSuccess: 'Import template downloaded.',
} as const
