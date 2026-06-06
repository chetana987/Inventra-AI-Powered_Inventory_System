import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="empty-state" [class.empty-state-sm]="compact()">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        @switch (icon()) {
          @case ('box') { <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/> }
          @case ('search') { <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/> }
          @case ('alert') { <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/> }
          @case ('activity') { <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/> }
          @case ('info') { <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/> }
          @default { <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/> }
        }
      </svg>
      <h3>{{ title() }}</h3>
      @if (description()) {
        <p>{{ description() }}</p>
      }
      @if (actionLabel() && actionRoute()) {
        <a [routerLink]="actionRoute()" class="btn-primary">{{ actionLabel() }}</a>
      }
      <ng-content />
    </div>
  `,
  styles: [`
    :host { display: contents; }
  `]
})
export class EmptyStateComponent {
  readonly icon = input<'box' | 'search' | 'alert' | 'activity' | 'info'>('info');
  readonly title = input('No data found');
  readonly description = input('');
  readonly actionLabel = input('');
  readonly actionRoute = input('');
  readonly compact = input(false);
}
