import {
  Alert,
  Card,
  CardContent,
  Divider,
  Link,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Skeleton,
  Stack,
  Typography,
} from '@mui/material'
import type { ReactNode } from 'react'
import { Link as RouterLink } from 'react-router-dom'
import { TruncatedTextWithTooltip } from '../common/TruncatedTextWithTooltip'
import { TEXT_DISPLAY_LIMITS } from '../common/textDisplay'

export interface DashboardListItem {
  id: string
  primary: ReactNode
  secondary?: ReactNode
  href?: string
}

interface DashboardListCardProps {
  title: string
  description?: string
  emptyText: string
  items: DashboardListItem[]
  loading?: boolean
  error?: string | null
  viewAllHref?: string
  viewAllLabel?: string
}

function renderDashboardListText(value: ReactNode, maxLength: number) {
  if (typeof value === 'string' || typeof value === 'number') {
    return <TruncatedTextWithTooltip maxLength={maxLength} text={String(value)} />
  }

  return value
}

export function DashboardListCard({
  description,
  emptyText,
  error,
  items,
  loading = false,
  title,
  viewAllHref,
  viewAllLabel = 'View all',
}: DashboardListCardProps) {
  return (
    <Card sx={{ height: '100%', minWidth: 0 }} variant="outlined">
      <CardContent sx={{ p: 2.5 }}>
        <Stack spacing={2} sx={{ height: '100%' }}>
          <Stack direction="row" spacing={1} sx={{ alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <Stack spacing={0.5} sx={{ minWidth: 0 }}>
              <Typography component="h3" sx={{ fontWeight: 700 }} variant="h6">
                {title}
              </Typography>
              {description ? (
                <Typography color="text.secondary" variant="body2">
                  {description}
                </Typography>
              ) : null}
            </Stack>
            {viewAllHref && !loading ? (
              <Link component={RouterLink} sx={{ flexShrink: 0, fontWeight: 600 }} to={viewAllHref} underline="hover" variant="body2">
                {viewAllLabel}
              </Link>
            ) : null}
          </Stack>
          {error ? <Alert severity="error">{error}</Alert> : null}
          {loading ? (
            <Stack spacing={1}>
              <Skeleton height={32} variant="rounded" />
              <Skeleton height={32} variant="rounded" />
              <Skeleton height={32} variant="rounded" />
            </Stack>
          ) : items.length === 0 ? (
            <Typography color="text.secondary" sx={{ py: 1 }} variant="body2">
              {emptyText}
            </Typography>
          ) : (
            <List disablePadding sx={{ flexGrow: 1 }}>
              {items.map((item, index) => (
                <div key={item.id}>
                  {item.href ? (
                    <ListItemButton component={RouterLink} disableGutters sx={{ minWidth: 0, px: 0, py: 1 }} to={item.href}>
                      <ListItemText
                        primary={renderDashboardListText(item.primary, TEXT_DISPLAY_LIMITS.listPrimary)}
                        secondary={
                          item.secondary
                            ? renderDashboardListText(item.secondary, TEXT_DISPLAY_LIMITS.listSecondary)
                            : undefined
                        }
                        slotProps={{
                          primary: { sx: { fontWeight: 600, minWidth: 0 } },
                          secondary: { sx: { minWidth: 0 } },
                        }}
                      />
                    </ListItemButton>
                  ) : (
                    <ListItem disableGutters sx={{ minWidth: 0, px: 0, py: 1 }}>
                      <ListItemText
                        primary={renderDashboardListText(item.primary, TEXT_DISPLAY_LIMITS.listPrimary)}
                        secondary={
                          item.secondary
                            ? renderDashboardListText(item.secondary, TEXT_DISPLAY_LIMITS.listSecondary)
                            : undefined
                        }
                        slotProps={{
                          primary: { sx: { fontWeight: 600, minWidth: 0 } },
                          secondary: { sx: { minWidth: 0 } },
                        }}
                      />
                    </ListItem>
                  )}
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
