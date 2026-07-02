import { Tab, Tabs } from '@mui/material'
import { TECHNOLOGY_STATUS_TABS } from '../../pages/learn/learnListParams'
import type { TechnologyListQuery } from '../../types/learn'

interface TechnologyStatusFilterTabsProps {
  value: TechnologyListQuery['status']
  onChange: (status: TechnologyListQuery['status']) => void
}

export function TechnologyStatusFilterTabs({ value, onChange }: TechnologyStatusFilterTabsProps) {
  return (
    <Tabs
      onChange={(_event, nextValue: TechnologyListQuery['status']) => onChange(nextValue)}
      sx={{ mb: 2 }}
      value={value}
    >
      {TECHNOLOGY_STATUS_TABS.map((tab) => (
        <Tab key={tab.label} label={tab.label} value={tab.value} />
      ))}
    </Tabs>
  )
}
