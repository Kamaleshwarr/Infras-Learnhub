export function truncateText(text: string, maxLength: number) {
  if (text.length <= maxLength) {
    return text
  }

  return `${text.slice(0, maxLength).trimEnd()}…`
}

export const TEXT_DISPLAY_LIMITS = {
  tableTitle: 60,
  tableReward: 60,
  tableName: 50,
  tableEmail: 60,
  tableEmployeeId: 40,
  tableFilename: 50,
  tableComments: 80,
  tableInitiative: 60,
  cardTitle: 80,
  cardReward: 80,
  listPrimary: 60,
  listSecondary: 80,
  notificationTitle: 80,
  notificationMessage: 120,
  menuItem: 80,
  headerSubtitle: 60,
} as const

export const fixedTableSx = {
  tableLayout: 'fixed',
  width: '100%',
} as const
