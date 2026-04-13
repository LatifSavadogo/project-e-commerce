import { apiJson } from './apiClient'

export type ChatbotReply = {
  reply: string
  intentsDetected?: string[]
  suggestions?: string[]
  transferSuggested?: boolean
}

export async function chatbotReply(message: string): Promise<ChatbotReply> {
  return apiJson<ChatbotReply>('/api/v1/chatbot/reply', {
    method: 'POST',
    body: JSON.stringify({ message: message.trim() }),
  })
}
