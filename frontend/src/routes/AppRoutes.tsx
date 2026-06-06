import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '../layout/AppLayout'
import { LoginPage } from '../pages/auth/LoginPage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { NotFoundPage } from '../pages/errors/NotFoundPage'
import { InitiativeDetailPage } from '../pages/initiatives/InitiativeDetailPage'
import { InitiativeListPage } from '../pages/initiatives/InitiativeListPage'
import { GlobalLeaderboardPage } from '../pages/leaderboards/GlobalLeaderboardPage'
import { InitiativeLeaderboardPage } from '../pages/leaderboards/InitiativeLeaderboardPage'
import { ProjectKnowledgePage } from '../pages/projects/ProjectKnowledgePage'
import { ProjectsPage } from '../pages/projects/ProjectsPage'
import { StudyMaterialsPage } from '../pages/studyMaterials/StudyMaterialsPage'
import { MySubmissionsPage } from '../pages/submissions/MySubmissionsPage'
import { SubmitCertificatePage } from '../pages/submissions/SubmitCertificatePage'
import { ProtectedRoute } from './ProtectedRoute'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<LoginPage />} path="/login" />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route element={<DashboardPage />} index />
          <Route element={<InitiativeListPage />} path="initiatives" />
          <Route element={<InitiativeDetailPage />} path="initiatives/:initiativeId" />
          <Route element={<SubmitCertificatePage />} path="submissions/new" />
          <Route element={<MySubmissionsPage />} path="submissions" />
          <Route element={<GlobalLeaderboardPage />} path="leaderboards/global" />
          <Route element={<InitiativeLeaderboardPage />} path="leaderboards/initiatives" />
          <Route element={<StudyMaterialsPage />} path="study-materials" />
          <Route element={<ProjectsPage />} path="projects" />
          <Route element={<ProjectKnowledgePage />} path="projects/:projectId" />
        </Route>
      </Route>
      <Route element={<Navigate replace to="/" />} path="/dashboard" />
      <Route element={<NotFoundPage />} path="*" />
    </Routes>
  )
}
