import { afterEach, describe, expect, it, vi } from 'vitest'
import { downloadBlob } from './downloadBlob'

describe('downloadBlob', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('creates a temporary download link', () => {
    const click = vi.fn()
    const anchor = { click, download: '' } as unknown as HTMLAnchorElement
    const createElement = vi.spyOn(document, 'createElement').mockReturnValue(anchor)
    const createObjectURL = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:mock')
    const revokeObjectURL = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined)

    downloadBlob(new Blob(['Employee ID,Full Name,Email,Role\n']), 'user-import-template.csv')

    expect(createElement).toHaveBeenCalledWith('a')
    expect(createObjectURL).toHaveBeenCalled()
    expect(anchor.download).toBe('user-import-template.csv')
    expect(click).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:mock')
  })
})
