import AddOutlinedIcon from '@mui/icons-material/AddOutlined'
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined'
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined'
import { Box, Button, CircularProgress, Stack } from '@mui/material'

interface UserListToolbarProps {
  onCreateUser: () => void
  onImportUsers: () => void
  onDownloadTemplate: () => void
  downloadingTemplate?: boolean
}

export function UserListToolbar({
  onCreateUser,
  onImportUsers,
  onDownloadTemplate,
  downloadingTemplate = false,
}: UserListToolbarProps) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
      <Stack direction="row" spacing={1}>
        <Button
          disabled={downloadingTemplate}
          onClick={onDownloadTemplate}
          startIcon={downloadingTemplate ? <CircularProgress color="inherit" size={18} /> : <FileDownloadOutlinedIcon />}
          variant="outlined"
        >
          Download Template
        </Button>
        <Button onClick={onImportUsers} startIcon={<UploadFileOutlinedIcon />} variant="outlined">
          Import Users
        </Button>
        <Button onClick={onCreateUser} startIcon={<AddOutlinedIcon />} variant="contained">
          Create User
        </Button>
      </Stack>
    </Box>
  )
}
