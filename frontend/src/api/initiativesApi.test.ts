import { beforeEach, describe, expect, it, vi } from 'vitest'
import { httpClient } from './httpClient'
import { initiativesApi } from './initiativesApi'
import type { Initiative } from '../types/initiatives'

vi.mock('./httpClient', () => ({
  httpClient: {
    delete: vi.fn(),
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
  },
}))

const initiative: Initiative = {
  createdAtUtc: '2026-01-01T00:00:00Z',
  createdBy: {
    email: 'admin@example.com',
    employeeId: 'ADMIN001',
    fullName: 'Admin User',
    id: 'admin-1',
  },
  description: 'AWS certification program',
  expiryDateUtc: '2026-12-31T00:00:00Z',
  id: '550e8400-e29b-41d4-a716-446655440001',
  rewardDescription: '$500 credit',
  startDateUtc: '2026-01-01T00:00:00Z',
  status: 'ACTIVE',
  title: 'AWS Certification',
  updatedAtUtc: '2026-06-01T00:00:00Z',
}

describe('initiativesApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists initiatives with query params', async () => {
    const responseData = {
      content: [initiative],
      first: true,
      last: true,
      page: 0,
      size: 20,
      sort: [],
      totalElements: 1,
      totalPages: 1,
    }
    vi.mocked(httpClient.get).mockResolvedValue({ data: responseData })

    const result = await initiativesApi.list({
      page: 0,
      search: 'aws',
      size: 20,
      sort: 'expiryDateUtc,asc',
      status: 'ACTIVE',
    })

    expect(httpClient.get).toHaveBeenCalledWith('/initiatives', {
      params: {
        page: 0,
        search: 'aws',
        size: 20,
        sort: 'expiryDateUtc,asc',
        status: 'ACTIVE',
      },
    })
    expect(result).toEqual(responseData)
  })

  it('gets an initiative by id', async () => {
    vi.mocked(httpClient.get).mockResolvedValue({ data: initiative })

    const result = await initiativesApi.get(initiative.id)

    expect(httpClient.get).toHaveBeenCalledWith(`/initiatives/${initiative.id}`)
    expect(result).toEqual(initiative)
  })

  it('creates an initiative', async () => {
    const request = {
      description: 'New program',
      expiryDateUtc: '2026-12-31T00:00:00.000Z',
      rewardDescription: '$500 credit',
      startDateUtc: '2026-01-01T00:00:00.000Z',
      status: 'DRAFT' as const,
      title: 'New Initiative',
    }
    vi.mocked(httpClient.post).mockResolvedValue({ data: initiative })

    const result = await initiativesApi.create(request)

    expect(httpClient.post).toHaveBeenCalledWith('/initiatives', request)
    expect(result).toEqual(initiative)
  })

  it('updates an initiative', async () => {
    const request = {
      description: 'Updated program',
      expiryDateUtc: '2026-12-31T00:00:00.000Z',
      rewardDescription: null,
      startDateUtc: '2026-01-01T00:00:00.000Z',
      status: 'ACTIVE' as const,
      title: 'Updated Initiative',
    }
    vi.mocked(httpClient.put).mockResolvedValue({ data: { ...initiative, ...request, status: 'ACTIVE' } })

    const result = await initiativesApi.update(initiative.id, request)

    expect(httpClient.put).toHaveBeenCalledWith(`/initiatives/${initiative.id}`, request)
    expect(result.title).toBe('Updated Initiative')
  })

  it('deletes an initiative', async () => {
    vi.mocked(httpClient.delete).mockResolvedValue({})

    await initiativesApi.delete(initiative.id)

    expect(httpClient.delete).toHaveBeenCalledWith(`/initiatives/${initiative.id}`)
  })
})
