import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '../layout/AppLayout'
import { ChangePasswordPage } from '../pages/auth/ChangePasswordPage'
import { ForgotPasswordPage } from '../pages/auth/ForgotPasswordPage'
import { LoginPage } from '../pages/auth/LoginPage'
import { ResetPasswordPage } from '../pages/auth/ResetPasswordPage'
import { DashboardPage } from '../pages/dashboard/DashboardPage'
import { NotFoundPage } from '../pages/errors/NotFoundPage'
import { LearnManagePage } from '../pages/learn/LearnManagePage'
import { LearnHomePage } from '../pages/learn/LearnHomePage'
import { TechnologyDetailPage } from '../pages/learn/TechnologyDetailPage'
import { RoadmapPage } from '../pages/learn/RoadmapPage'
import { TechnologyListPage } from '../pages/learn/TechnologyListPage'
import { LearnLayout } from '../layout/LearnLayout'
import { InitiativeDetailPage } from '../pages/initiatives/InitiativeDetailPage'
import { InitiativeListPage } from '../pages/initiatives/InitiativeListPage'
import { GlobalLeaderboardPage } from '../pages/leaderboards/GlobalLeaderboardPage'
import { InitiativeLeaderboardPage } from '../pages/leaderboards/InitiativeLeaderboardPage'
import { ProjectKnowledgePage } from '../pages/projects/ProjectKnowledgePage'
import { ProjectsPage } from '../pages/projects/ProjectsPage'
import { StudyMaterialsPage } from '../pages/studyMaterials/StudyMaterialsPage'
import { AdminReviewPage } from '../pages/submissions/AdminReviewPage'
import { MySubmissionsPage } from '../pages/submissions/MySubmissionsPage'
import { SubmitCertificatePage } from '../pages/submissions/SubmitCertificatePage'
import { UserListPage } from '../pages/users/UserListPage'
import { ProfilePage } from '../pages/profile/ProfilePage'
import { NotificationsPage } from '../pages/notifications/NotificationsPage'
import { MustChangePasswordRoute } from './MustChangePasswordRoute'
import { ProtectedRoute } from './ProtectedRoute'
import { RoleRoute } from './RoleRoute'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<LoginPage />} path="/login" />
      <Route element={<ForgotPasswordPage />} path="/forgot-password" />
      <Route element={<ResetPasswordPage />} path="/reset-password" />
      <Route element={<ProtectedRoute />}>
        <Route element={<MustChangePasswordRoute />}>
          <Route element={<ChangePasswordPage />} path="/change-password" />
          <Route element={<AppLayout />}>
            <Route element={<DashboardPage />} index />
            <Route element={<ProfilePage />} path="profile" />
            <Route element={<NotificationsPage />} path="notifications" />
            <Route element={<LearnLayout />}>
              <Route element={<LearnHomePage />} path="learn" />
              <Route element={<TechnologyListPage />} path="learn/technologies" />
              <Route element={<TechnologyDetailPage />} path="learn/technologies/:technologyId" />
              <Route element={<RoadmapPage />} path="learn/technologies/:technologyId/roadmap" />
              <Route element={<RoleRoute roles={['ADMIN']} />}>
                <Route element={<LearnManagePage />} path="learn/manage" />
                <Route element={<TechnologyListPage adminMode />} path="learn/manage/technologies" />
              </Route>
            </Route>
            <Route element={<InitiativeListPage />} path="initiatives" />
            <Route element={<InitiativeDetailPage />} path="initiatives/:initiativeId" />
            <Route element={<RoleRoute roles={['EMPLOYEE']} />}>
              <Route element={<SubmitCertificatePage />} path="submissions/new" />
              <Route element={<MySubmissionsPage />} path="submissions" />
            </Route>
            <Route element={<RoleRoute roles={['ADMIN']} />}>
              <Route element={<UserListPage />} path="users" />
              <Route element={<AdminReviewPage />} path="submissions/review" />
            </Route>
            <Route element={<GlobalLeaderboardPage />} path="leaderboards/global" />
            <Route element={<InitiativeLeaderboardPage />} path="leaderboards/initiatives/:initiativeId" />
            <Route element={<StudyMaterialsPage />} path="study-materials" />
            <Route element={<ProjectsPage />} path="projects" />
            <Route element={<ProjectKnowledgePage />} path="projects/:projectId" />
          </Route>
        </Route>
      </Route>
      <Route element={<Navigate replace to="/" />} path="/dashboard" />
      <Route element={<NotFoundPage />} path="*" />
    </Routes>
  )
}
