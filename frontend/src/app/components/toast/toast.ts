import { Component, effect } from '@angular/core';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-container">
      @for (toast of service.toastsSignal(); track toast.id) {
        <div class="toast" [class.toast-leaving]="toast.leaving" [class.toast-success]="toast.type === 'success'" [class.toast-error]="toast.type === 'error'" [class.toast-warning]="toast.type === 'warning'" [class.toast-info]="toast.type === 'info'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            @switch (toast.type) {
              @case ('success') { <polyline points="20 6 9 17 4 12"/> }
              @case ('error') { <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/> }
              @case ('warning') { <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/> }
              @case ('info') { <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/> }
            }
          </svg>
          <span>{{ toast.message }}</span>
          <button class="toast-close" (click)="service.dismiss(toast.id)" aria-label="Dismiss">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    :host { display: contents; }
  `]
})
export class ToastComponent {
  constructor(public service: ToastService) {}
}
