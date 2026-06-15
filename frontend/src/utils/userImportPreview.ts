const IMPORT_FILE_EXTENSIONS = ['.csv', '.xls', '.xlsx'] as const

export function isAcceptedImportFile(file: File) {
  const filename = file.name.toLowerCase()
  return IMPORT_FILE_EXTENSIONS.some((extension) => filename.endsWith(extension))
}

function splitCsvLine(line: string) {
  const values: string[] = []
  let current = ''
  let quoted = false

  for (const character of line) {
    if (character === '"') {
      quoted = !quoted
      continue
    }
    if (character === ',' && !quoted) {
      values.push(current.trim())
      current = ''
      continue
    }
    current += character
  }

  values.push(current.trim())
  return values
}

function isImportHeader(columns: string[]) {
  return (
    columns.length >= 4 &&
    columns[0]?.toLowerCase() === 'employee id' &&
    columns[1]?.toLowerCase() === 'full name' &&
    columns[2]?.toLowerCase() === 'email' &&
    columns[3]?.toLowerCase() === 'role'
  )
}

function countCsvDataRows(content: string) {
  let count = 0

  for (const line of content.split(/\r?\n/)) {
    if (!line.trim()) {
      continue
    }
    const columns = splitCsvLine(line)
    if (isImportHeader(columns)) {
      continue
    }
    count += 1
  }

  return count
}

export async function estimateImportRowCount(file: File): Promise<number | null> {
  if (!file.name.toLowerCase().endsWith('.csv')) {
    return null
  }

  const content = await file.text()
  return countCsvDataRows(content)
}
