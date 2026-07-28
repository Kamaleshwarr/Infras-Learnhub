import SmartToyOutlinedIcon from '@mui/icons-material/SmartToyOutlined'
import { Fab } from '@mui/material'
import { useState } from 'react'
import { useAssistant } from '../../assistant/useAssistant'
import { AssistantChatPanel } from './AssistantChatPanel'
import { assistantMessages } from './assistantMessages'

export function AssistantWidget() {
  const { enabled, loading } = useAssistant()
  const [panelOpen, setPanelOpen] = useState(false)

  if (loading || !enabled) {
    return null
  }

  return (
    <>
      <Fab
        aria-label={assistantMessages.open}
        color="primary"
        onClick={() => setPanelOpen((current) => !current)}
        sx={{
          position: 'fixed',
          right: { xs: 16, sm: 24 },
          bottom: { xs: 16, sm: 24 },
          zIndex: (theme) => theme.zIndex.modal + 1,
        }}
      >
        <SmartToyOutlinedIcon />
      </Fab>
      <AssistantChatPanel onClose={() => setPanelOpen(false)} open={panelOpen} />
    </>
  )
}
