import DashboardOutlinedIcon from '@mui/icons-material/DashboardOutlined'
import EmojiEventsOutlinedIcon from '@mui/icons-material/EmojiEventsOutlined'
import FolderOpenOutlinedIcon from '@mui/icons-material/FolderOpenOutlined'
import LibraryBooksOutlinedIcon from '@mui/icons-material/LibraryBooksOutlined'
import SchoolOutlinedIcon from '@mui/icons-material/SchoolOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
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
  { label: 'Initiatives', path: '/initiatives', icon: SchoolOutlinedIcon },
  { label: 'Submit Certificate', path: '/submissions/new', icon: UploadFileOutlinedIcon },
  { label: 'My Submissions', path: '/submissions', icon: LibraryBooksOutlinedIcon },
  { label: 'Leaderboards', path: '/leaderboards/global', icon: EmojiEventsOutlinedIcon },
  { label: 'Study Materials', path: '/study-materials', icon: FolderOpenOutlinedIcon },
  { label: 'Projects', path: '/projects', icon: WorkspacesOutlinedIcon },
]
