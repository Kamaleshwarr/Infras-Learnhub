import { Button, Card, CardContent, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { PageHeader } from '../../components/common/PageHeader'
import { LEARN_MESSAGES } from '../../components/learn/learnMessages'

export function LearnManagePage() {
  return (
    <>
      <PageHeader description={LEARN_MESSAGES.manageDescription} title={LEARN_MESSAGES.manageTitle} />
      <Stack spacing={2}>
        <Card variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Typography variant="h6">{LEARN_MESSAGES.manageTechnologiesTitle}</Typography>
              <Typography color="text.secondary">{LEARN_MESSAGES.manageTechnologiesDescription}</Typography>
              <Button component={RouterLink} to="/learn/manage/technologies" variant="contained">
                Manage Technologies
              </Button>
            </Stack>
          </CardContent>
        </Card>
      </Stack>
    </>
  )
}
