import { describe, expect, it } from 'vitest'
import {
  buildTechnologyCreateRequest,
  createEmptyTechnologyForm,
  getTechnologyFormFieldErrors,
} from '../../components/learn/learnFormState'

describe('learnFormState', () => {
  it('validates required fields', () => {
    const errors = getTechnologyFormFieldErrors(createEmptyTechnologyForm())
    expect(errors.name).toBeTruthy()
    expect(errors.shortName).toBeTruthy()
    expect(errors.category).toBeTruthy()
    expect(errors.difficulty).toBeTruthy()
  })

  it('builds create request from valid form', () => {
    const request = buildTechnologyCreateRequest({
      name: 'Spring Boot',
      shortName: 'Spring Boot',
      description: 'Java framework',
      category: 'LANGUAGES',
      difficulty: 'INTERMEDIATE',
      featured: false,
      linkedProjectIds: [],
    })

    expect(request).toEqual({
      name: 'Spring Boot',
      shortName: 'Spring Boot',
      description: 'Java framework',
      category: 'LANGUAGES',
      difficulty: 'INTERMEDIATE',
    })
  })
})
