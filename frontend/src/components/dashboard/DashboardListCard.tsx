import {
  Alert,
  Card,
  CardContent,
  Divider,
  List,
  ListItem,
  ListItemText,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import type { ReactNode } from 'react'

export interface DashboardListItem {
  id: string
  primary: ReactNode
  secondary?: ReactNode
}

interface DashboardListCardProps {
  title: string
  emptyText: string
  items: DashboardListItem[]
  loading?: boolean
  error?: string | null
}

export function DashboardListCard({ emptyText, error, items, loading = false, title }: DashboardListCardProps) {
  return (
    <Card sx={{ height: '100%' }} variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Typography variant="h6">{title}</Typography>
          {error ? <Alert severity="error">{error}</Alert> : null}
          {loading ? (
            <Stack spacing={1}>
              <Skeleton height={32} />
              <Skeleton height={32} />
              <Skeleton height={32} />
            </Stack>
          ) : items.length === 0 ? (
            <Typography color="text.secondary" variant="body2">
              {emptyText}
            </Typography>
          ) : (
            <List disablePadding>
              {items.map((item, index) => (
                <div key={item.id}>
                  <ListItem disableGutters>
                    <ListItemText primary={item.primary} secondary={item.secondary} />
                  </ListItem>
                  {index < items.length - 1 ? <Divider component="li" /> : null}
                </div>
              ))}
            </List>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}

