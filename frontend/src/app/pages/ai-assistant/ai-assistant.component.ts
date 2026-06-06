import { Component, OnInit, ElementRef, ViewChild, AfterViewChecked, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../services/ai.service';
import { ChatSession, ChatMessage } from '../../models/ai';

@Component({
  selector: 'app-ai-assistant',
  imports: [FormsModule],
  templateUrl: './ai-assistant.component.html',
  styleUrl: './ai-assistant.component.scss'
})
export class AiAssistantComponent implements OnInit, AfterViewChecked {
  @ViewChild('chatContainer') private chatContainer!: ElementRef;

  sessions: ChatSession[] = [];
  currentSessionId: string | null = null;
  input = '';
  loading = signal(false);
  sidebarOpen = signal(false);

  suggestions = [
    { icon: 'alert-triangle', label: 'Which products are low in stock?', text: 'Which products are low in stock?' },
    { icon: 'package', label: 'Show products below quantity 20', text: 'Show products below quantity 20' },
    { icon: 'trending-up', label: 'What are the most active products?', text: 'What are the most active products?' },
    { icon: 'clock', label: 'Show recent transactions', text: 'Show recent transactions' }
  ];

  constructor(private ai: AiService) {}

  ngOnInit(): void {
    this.newSession();
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  get currentSession(): ChatSession | undefined {
    return this.sessions.find(s => s.id === this.currentSessionId);
  }

  get messages(): ChatMessage[] {
    return this.currentSession?.messages ?? [];
  }

  newSession(): void {
    const session: ChatSession = {
      id: crypto.randomUUID(),
      title: 'New Chat',
      messages: [],
      createdAt: new Date()
    };
    this.sessions.unshift(session);
    this.currentSessionId = session.id;
    if (window.innerWidth <= 768) this.sidebarOpen.set(false);
  }

  selectSession(id: string): void {
    this.currentSessionId = id;
    if (window.innerWidth <= 768) this.sidebarOpen.set(false);
  }

  deleteSession(id: string, event: Event): void {
    event.stopPropagation();
    this.sessions = this.sessions.filter(s => s.id !== id);
    if (this.currentSessionId === id) {
      this.currentSessionId = this.sessions[0]?.id ?? null;
      if (!this.currentSessionId) this.newSession();
    }
  }

  clearCurrentChat(): void {
    const session = this.currentSession;
    if (session) {
      session.messages = [];
      session.title = 'New Chat';
    }
  }

  send(text?: string): void {
    const msg = text ?? this.input.trim();
    if (!msg || this.loading()) return;

    if (this.currentSession?.title === 'New Chat') {
      this.currentSession!.title = msg.length > 40 ? msg.slice(0, 40) + '…' : msg;
    }

    this.currentSession?.messages.push({ role: 'user', content: msg, timestamp: new Date() });
    this.input = '';
    this.loading.set(true);

    this.ai.query(msg).subscribe({
      next: res => {
        const d = res.data;
        this.currentSession?.messages.push({
          role: 'assistant',
          content: d.summary,
          data: d.data,
          timestamp: new Date()
        });
        this.loading.set(false);
      },
      error: () => {
        this.currentSession?.messages.push({
          role: 'assistant',
          content: 'Sorry, I encountered an error processing your request. Please try again.',
          timestamp: new Date()
        });
        this.loading.set(false);
      }
    });
  }

  private scrollToBottom(): void {
    try {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    } catch {}
  }
}
