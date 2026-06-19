import { Box, Skeleton, Stack } from '@mui/material'

export function DetailPageSkeleton() {
  return (
    <Stack spacing={3}>
      <Box>
        <Skeleton height={40} width="60%" />
        <Skeleton height={24} sx={{ mt: 1 }} width="40%" />
      </Box>
      <Box
        sx={{
          display: 'grid',
          gap: 3,
          gridTemplateColumns: { md: '2fr 1fr', xs: '1fr' },
        }}
      >
        <Stack spacing={2}>
          <Skeleton height={160} variant="rounded" />
          <Skeleton height={100} variant="rounded" />
        </Stack>
        <Stack spacing={2}>
          <Skeleton height={120} variant="rounded" />
          <Skeleton height={120} variant="rounded" />
        </Stack>
      </Box>
    </Stack>
  )
}
