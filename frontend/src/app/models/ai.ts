export interface AiQueryRequest {
  question: string;
}

export interface AiQueryResponse {
  question: string;
  intent: string;
  summary: string;
  data: any;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  data?: any;
  timestamp: Date;
}

export interface ChatSession {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: Date;
}
