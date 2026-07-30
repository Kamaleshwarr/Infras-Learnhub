import {
  Alert,
  Box,
  Card,
  CardContent,
  Divider,
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
import { cardContentPadding, emptyStateSx } from '../../theme/uiTokens'

export interface DashboardListItem {
  id: string
  primary: ReactNode
  secondary?: ReactNode
  href?: string
}

interface DashboardListCardProps {
  title: string
  emptyText: string
  items: DashboardListItem[]
  loading?: boolean
  error?: string | null
}

function renderDashboardListText(value: ReactNode, maxLength: number) {
  if (typeof value === 'string' || typeof value === 'number') {
    return <TruncatedTextWithTooltip maxLength={maxLength} text={String(value)} />
  }

  return value
}

const listItemButtonSx = {
  borderRadius: 1,
  px: 1,
  py: 0.75,
  minWidth: 0,
  transition: 'background-color 0.15s ease',
  '&:hover': {
    bgcolor: 'action.hover',
  },
} as const

export function DashboardListCard({ emptyText, error, items, loading = false, title }: DashboardListCardProps) {
  return (
    <Card sx={{ height: '100%', minWidth: 0 }} variant="outlined">
      <CardContent sx={cardContentPadding}>
        <Stack spacing={2}>
          <Typography sx={{ fontWeight: 700 }} variant="h6">{title}</Typography>
          {error ? <Alert severity="error">{error}</Alert> : null}
          {loading ? (
            <Stack spacing={1}>
              <Skeleton height={32} />
              <Skeleton height={32} />
              <Skeleton height={32} />
            </Stack>
          ) : items.length === 0 ? (
            <Box sx={emptyStateSx}>
              <Typography color="text.secondary" variant="body2">
                {emptyText}
              </Typography>
            </Box>
          ) : (
            <List disablePadding sx={{ mx: -0.5 }}>
              {items.map((item, index) => (
                <div key={item.id}>
                  {item.href ? (
                    <ListItemButton component={RouterLink} disableGutters sx={listItemButtonSx} to={item.href}>
                      <ListItemText
                        primary={renderDashboardListText(item.primary, TEXT_DISPLAY_LIMITS.listPrimary)}
                        secondary={
                          item.secondary
                            ? renderDashboardListText(item.secondary, TEXT_DISPLAY_LIMITS.listSecondary)
                            : undefined
                        }
                        slotProps={{
                          primary: { sx: { minWidth: 0, fontWeight: 500 } },
                          secondary: { sx: { minWidth: 0 } },
                        }}
                      />
                    </ListItemButton>
                  ) : (
                    <ListItem disableGutters sx={{ ...listItemButtonSx, px: 1 }}>
                      <ListItemText
                        primary={renderDashboardListText(item.primary, TEXT_DISPLAY_LIMITS.listPrimary)}
                        secondary={
                          item.secondary
                            ? renderDashboardListText(item.secondary, TEXT_DISPLAY_LIMITS.listSecondary)
                            : undefined
                        }
                        slotProps={{
                          primary: { sx: { minWidth: 0, fontWeight: 500 } },
                          secondary: { sx: { minWidth: 0 } },
                        }}
                      />
                    </ListItem>
                  )}
                  {index < items.length - 1 ? <Divider component="li" sx={{ my: 0.25 }} /> : null}
                </div>
              ))}
            </List>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}
