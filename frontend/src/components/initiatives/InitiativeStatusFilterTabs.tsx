import { Box, Tab, Tabs } from '@mui/material'
import { INITIATIVE_STATUS_TABS } from '../../pages/initiatives/initiativeListParams'
import type { InitiativeListQuery } from '../../types/initiatives'

interface InitiativeStatusFilterTabsProps {
  value: InitiativeListQuery['status']
  onChange: (status: InitiativeListQuery['status']) => void
}

export function InitiativeStatusFilterTabs({ value, onChange }: InitiativeStatusFilterTabsProps) {
  return (
    <Box sx={{ mb: 2 }}>
      <Tabs
        onChange={(_event, nextValue: InitiativeListQuery['status']) => onChange(nextValue)}
        value={value}
      >
        {INITIATIVE_STATUS_TABS.map((tab) => (
          <Tab key={tab.label} label={tab.label} value={tab.value} />
        ))}
      </Tabs>
    </Box>
  )
}
