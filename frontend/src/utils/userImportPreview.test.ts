import { describe, expect, it } from 'vitest'
import { estimateImportRowCount, isAcceptedImportFile } from './userImportPreview'

describe('userImportPreview', () => {
  it('accepts csv, xls, and xlsx files', () => {
    expect(isAcceptedImportFile(new File([''], 'users.csv'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.xls'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.xlsx'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.txt'))).toBe(false)
  })

  it('counts csv data rows and skips the header', async () => {
    const file = new File(
      [
        'Employee ID,Full Name,Email,Role\n',
        'EMP010,Jane Doe,jane@example.com,EMPLOYEE\n',
        'EMP011,John Doe,john@example.com,EMPLOYEE\n',
      ],
      'users.csv',
      { type: 'text/csv' },
    )

    await expect(estimateImportRowCount(file)).resolves.toBe(2)
  })

  it('returns null for spreadsheet files', async () => {
    const file = new File(['binary'], 'users.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })

    await expect(estimateImportRowCount(file)).resolves.toBeNull()
  })

  it('returns zero for an empty csv file', async () => {
    const file = new File([''], 'empty.csv', { type: 'text/csv' })

    await expect(estimateImportRowCount(file)).resolves.toBe(0)
  })
})
