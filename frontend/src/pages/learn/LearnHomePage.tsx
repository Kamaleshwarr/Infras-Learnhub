import { useCallback, useEffect, useMemo, useState } from 'react'
import SearchOutlinedIcon from '@mui/icons-material/SearchOutlined'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Grid,
  InputAdornment,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { learnApi } from '../../api/learnApi'
import { PageHeader } from '../../components/common/PageHeader'
import { FeaturedTechnologyCard } from '../../components/learn/FeaturedTechnologyCard'
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

      <Card sx={{ mb: 3 }} variant="outlined">
        <CardContent sx={{ py: 2.5 }}>
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
            <Button component={RouterLink} sx={{ alignSelf: 'flex-start' }} to="/learn/technologies" variant="outlined">
              {LEARN_MESSAGES.homeBrowseAll}
            </Button>
          </Stack>
        </CardContent>
      </Card>

      <Box component="section">
        <Typography component="h2" sx={{ fontWeight: 600, mb: 2 }} variant="h6">
          {LEARN_MESSAGES.homeFeaturedTitle}
        </Typography>

        {error ? <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert> : null}
        {!loading && !error && featuredToShow.length === 0 ? (
          <Alert severity="info">{LEARN_MESSAGES.homeEmptyFeatured}</Alert>
        ) : null}

        <Grid container spacing={2.5}>
          {featuredToShow.map((technology) => (
            <Grid key={technology.id} size={{ xs: 12, sm: 6, md: 4 }}>
              <FeaturedTechnologyCard technology={technology} />
            </Grid>
          ))}
        </Grid>
      </Box>
    </>
  )
}
