import AutoStoriesOutlinedIcon from '@mui/icons-material/AutoStoriesOutlined'
import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined'
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import LibraryBooksOutlinedIcon from '@mui/icons-material/LibraryBooksOutlined'
import NotificationsOutlinedIcon from '@mui/icons-material/NotificationsOutlined'
import PersonOutlinedIcon from '@mui/icons-material/PersonOutlined'
import SchoolOutlinedIcon from '@mui/icons-material/SchoolOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import PendingActionsOutlinedIcon from '@mui/icons-material/PendingActionsOutlined'
import PeopleOutlinedIcon from '@mui/icons-material/PeopleOutlined'
import WorkspacesOutlinedIcon from '@mui/icons-material/WorkspacesOutlined'
import type { SvgIconComponent } from '@mui/icons-material'
import type { UserRole } from '../types/auth'

export interface NavigationItem {
  label: string
  path: string
  icon: SvgIconComponent
  roles?: UserRole[]
}

export const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', path: '/', icon: DashboardOutlinedIcon },
  { label: 'My Profile', path: '/profile', icon: PersonOutlinedIcon },
  { label: 'Notifications', path: '/notifications', icon: NotificationsOutlinedIcon },
  { label: 'Users', path: '/users', icon: PeopleOutlinedIcon, roles: ['ADMIN'] },
  { label: 'Review Submissions', path: '/submissions/review', icon: PendingActionsOutlinedIcon, roles: ['ADMIN'] },
  { label: 'Learn', path: '/learn', icon: AutoStoriesOutlinedIcon },
  { label: 'Initiatives', path: '/initiatives', icon: SchoolOutlinedIcon },
  { label: 'Submit Certificate', path: '/submissions/new', icon: UploadFileOutlinedIcon, roles: ['EMPLOYEE'] },
  { label: 'My Certifications', path: '/submissions', icon: LibraryBooksOutlinedIcon, roles: ['EMPLOYEE'] },
  { label: 'Leaderboards', path: '/leaderboards/global', icon: EmojiEventsOutlinedIcon },
  { label: 'Study Materials', path: '/study-materials', icon: FolderOpenOutlinedIcon },
  { label: 'Projects', path: '/projects', icon: WorkspacesOutlinedIcon },
]
