import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { InitiativeLeaderboardPage } from './InitiativeLeaderboardPage'

describe('InitiativeLeaderboardPage', () => {
  it('renders initiative id from route params', () => {
    render(
      <MemoryRouter initialEntries={['/leaderboards/initiatives/initiative-123']}>
        <Routes>
          <Route element={<InitiativeLeaderboardPage />} path="/leaderboards/initiatives/:initiativeId" />
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: 'Initiative Leaderboard' })).toBeInTheDocument()
    expect(screen.getByText('Initiative ID: initiative-123')).toBeInTheDocument()
    expect(screen.getByText(/Ranking within initiative initiative-123/i)).toBeInTheDocument()
  })
})
