export const assistantMessages = {
  title: 'AI Assistant',
  open: 'Open AI Assistant',
  close: 'Close assistant',
  disabledTitle: 'AI Assistant unavailable',
  disabledDescription:
    'The AI assistant is not enabled in this deployment. Contact your administrator if you expected this feature.',
  emptyTitle: 'How can I help?',
  emptyDescription:
    'Ask about Engineering Learning Hub workflows, learning initiatives, or general technology topics.',
  placeholder: 'Ask a question…',
  send: 'Send message',
  loadingHistory: 'Loading conversation…',
  thinking: 'Assistant is thinking…',
  sendError: 'Unable to send your message. Please try again.',
  historyError: 'Unable to load conversation history.',
  statusError: 'Unable to check assistant availability.',
  navigateTo: (label: string) => `Go to ${label}`,
  examples: [
    'What is Docker?',
    'My certifications',
    'Open Learn',
  ],
} as const
