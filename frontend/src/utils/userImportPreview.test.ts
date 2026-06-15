import { describe, expect, it } from 'vitest'
import { estimateImportRowCount, isAcceptedImportFile, isBlankImportRow, normalizeImportCell } from './userImportPreview'

describe('userImportPreview', () => {
  it('accepts csv, xls, and xlsx files', () => {
    expect(isAcceptedImportFile(new File([''], 'users.csv'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.xls'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.xlsx'))).toBe(true)
    expect(isAcceptedImportFile(new File([''], 'users.txt'))).toBe(false)
  })

  it('normalizes invisible whitespace characters', () => {
    expect(normalizeImportCell(' \u00a0\u200b ')).toBe('')
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

  it('ignores comma-only and whitespace-only trailing csv rows', async () => {
    const file = new File(
      [
        'Employee ID,Full Name,Email,Role\n',
        'EMP010,Jane Doe,jane@example.com,EMPLOYEE\n',
        ',,,\n',
        ' , , , \n',
        '\n',
      ],
      'users.csv',
      { type: 'text/csv' },
    )

    await expect(estimateImportRowCount(file)).resolves.toBe(1)
  })

  it('ignores hash-prefixed comment lines in csv files', async () => {
    const file = new File(
      [
        'Employee ID,Full Name,Email,Role\n',
        '# Notes are ignored when users add their own comment lines\n',
        'EMP010,Jane Doe,jane@example.com,EMPLOYEE\n',
      ],
      'users.csv',
      { type: 'text/csv' },
    )

    await expect(estimateImportRowCount(file)).resolves.toBe(1)
  })

  it('counts partially populated rows', () => {
    expect(isBlankImportRow(['EMP010', 'Jane Doe', '', 'EMPLOYEE'])).toBe(false)
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
