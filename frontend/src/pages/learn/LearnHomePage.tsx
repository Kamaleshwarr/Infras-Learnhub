import { useCallback, useEffect, useMemo, useState } from 'react'
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActionArea,
  CardContent,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { PageHeader } from '../../components/common/PageHeader'
import { TechnologyCategoryChip, TechnologyDifficultyChip } from '../../components/learn/TechnologyMetaChips'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'
import { LearnPageIntro } from '../../layout/LearnLayout'
import type { Technology } from '../../types/learn'

export function LearnHomePage() {
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [featured, setFeatured] = useState<Technology[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadFeatured = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await learnApi.listTechnologies({ size: 12, sort: 'name,asc' })
      setFeatured(response.content.filter((technology) => technology.featured))
    } catch {
      setError(LEARN_MESSAGES.listLoadError)
      setFeatured([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadFeatured()
  }, [loadFeatured])

  const featuredToShow = useMemo(() => {
    if (featured.length > 0) {
      return featured
    }
    return []
  }, [featured])

  function handleSearchSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const params = new URLSearchParams()
    if (search.trim()) {
      params.set('search', search.trim())
    }
    navigate(`/learn/technologies?${params.toString()}`)
  }

  return (
    <>
      <PageHeader description={LEARN_MESSAGES.moduleDescription} title={LEARN_MESSAGES.moduleTitle} />
      <LearnPageIntro />

      <Card sx={{ mb: 4 }} variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h5">{LEARN_MESSAGES.homeHeroTitle}</Typography>
            <Typography color="text.secondary">{LEARN_MESSAGES.homeHeroDescription}</Typography>
            <Box component="form" onSubmit={handleSearchSubmit}>
              <TextField
                fullWidth
                onChange={(event) => setSearch(event.target.value)}
                placeholder={LEARN_MESSAGES.homeSearchPlaceholder}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchOutlinedIcon />
                      </InputAdornment>
                    ),
                  },
                }}
                value={search}
              />
            </Box>
            <Button component={RouterLink} to="/learn/technologies" variant="outlined">
              {LEARN_MESSAGES.homeBrowseAll}
            </Button>
          </Stack>
        </CardContent>
      </Card>

      <Stack spacing={2}>
        <Typography variant="h6">{LEARN_MESSAGES.homeFeaturedTitle}</Typography>
        {error ? <Alert severity="error">{error}</Alert> : null}
        {!loading && !error && featuredToShow.length === 0 ? (
          <Alert severity="info">{LEARN_MESSAGES.homeEmptyFeatured}</Alert>
        ) : null}
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ flexWrap: 'wrap' }}>
          {featuredToShow.map((technology) => (
            <Box key={technology.id} sx={{ flex: { xs: '1 1 100%', sm: '1 1 calc(50% - 16px)', md: '1 1 calc(33.333% - 16px)' } }}>
              <Card variant="outlined">
                <CardActionArea component={RouterLink} to={`/learn/technologies/${technology.id}`}>
                  <CardContent>
                    <Stack spacing={1}>
                      <Typography variant="h6">{technology.name}</Typography>
                      <Typography color="text.secondary" variant="body2">
                        {technology.shortName}
                      </Typography>
                      <Stack direction="row" spacing={1}>
                        <TechnologyCategoryChip category={technology.category} />
                        <TechnologyDifficultyChip difficulty={technology.difficulty} />
                      </Stack>
                    </Stack>
                  </CardContent>
                </CardActionArea>
              </Card>
            </Box>
          ))}
        </Stack>
      </Stack>
    </>
  )
}
