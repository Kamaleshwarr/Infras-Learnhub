import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { Box, Container } from '@mui/material'
import { Header } from './Header'
import { Sidebar, drawerWidth } from './Sidebar'

export function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar mobileOpen={mobileOpen} onClose={() => setMobileOpen(false)} />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          minWidth: 0,
          width: { md: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        <Header onMenuClick={() => setMobileOpen(true)} />
        <Container maxWidth="xl" sx={{ py: 4 }}>
          <Outlet />
        </Container>
      </Box>
    </Box>
  )
}
