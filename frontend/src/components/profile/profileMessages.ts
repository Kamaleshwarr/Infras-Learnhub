export const PROFILE_MESSAGES = {
  updateSuccess: 'Profile updated successfully.',
  updateError: 'Unable to update profile. Please try again.',
  avatarUploadSuccess: 'Avatar updated successfully.',
  avatarDeleteSuccess: 'Avatar removed successfully.',
  avatarUploadError: 'Unable to update avatar. Please try again.',
  avatarDeleteError: 'Unable to remove avatar. Please try again.',
  avatarValidationError: 'Avatar must be a JPG, JPEG, PNG, or WebP image up to 2 MB.',
} as const

export const AVATAR_MAX_SIZE_BYTES = 2 * 1024 * 1024

export const AVATAR_ACCEPT = 'image/jpeg,image/png,image/webp,.jpg,.jpeg,.png,.webp'
