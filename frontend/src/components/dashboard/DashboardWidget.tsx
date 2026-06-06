import { Alert, Box, Card, CardContent, Skeleton, Stack, Typography } from '@mui/material'
import type { ReactNode } from 'react'

interface DashboardWidgetProps {
  title: string
  value: string
  helperText: string
  icon?: ReactNode
  loading?: boolean
  error?: string | null
}

export function DashboardWidget({ error, helperText, icon, loading = false, title, value }: DashboardWidgetProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent>
        <Stack spacing={1.5}>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography color="text.secondary" variant="body2">
              {title}
            </Typography>
            {icon ? <Box sx={{ color: 'primary.main' }}>{icon}</Box> : null}
          </Stack>
          {loading ? <Skeleton height={48} width={96} /> : <Typography variant="h4">{value}</Typography>}
          <Typography color="text.secondary" variant="body2">
            {helperText}
          </Typography>
          {error ? <Alert severity="error">{error}</Alert> : null}
        </Stack>
      </CardContent>
    </Card>
  )
}
