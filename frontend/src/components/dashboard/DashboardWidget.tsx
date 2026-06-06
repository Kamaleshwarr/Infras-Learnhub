import { Card, CardContent, Stack, Typography } from '@mui/material'

interface DashboardWidgetProps {
  title: string
  value: string
  helperText: string
}

export function DashboardWidget({ title, value, helperText }: DashboardWidgetProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent>
        <Stack spacing={1}>
          <Typography color="text.secondary" variant="body2">
            {title}
          </Typography>
          <Typography variant="h4">{value}</Typography>
          <Typography color="text.secondary" variant="body2">
            {helperText}
          </Typography>
        </Stack>
      </CardContent>
    </Card>
  )
}
