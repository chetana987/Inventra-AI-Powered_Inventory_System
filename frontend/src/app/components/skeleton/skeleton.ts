import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  template: `
    @switch (variant()) {
      @case ('table-row') {
        <tr>
          @for (col of columns(); track $index) {
            <td><div class="skeleton skeleton-text" style="width: {{ col }}%"></div></td>
          }
        </tr>
      }
      @case ('card') {
        <div class="card">
          <div class="card-body">
            <div class="skeleton skeleton-text-lg" style="margin-bottom:12px"></div>
            <div class="skeleton skeleton-text" style="width:75%"></div>
            <div class="skeleton skeleton-text" style="width:50%;margin-top:8px"></div>
          </div>
        </div>
      }
      @case ('stat-card') {
        <div class="stat-card-skeleton">
          <div class="skeleton skeleton-circle" style="width:48px;height:48px;border-radius:12px"></div>
          <div style="flex:1">
            <div class="skeleton skeleton-text" style="width:60px"></div>
            <div class="skeleton skeleton-text-lg" style="width:80px;margin-top:6px"></div>
          </div>
        </div>
      }
      @default {
        <div class="skeleton-block">
          <div class="skeleton skeleton-text-lg" style="width:40%"></div>
          <div class="skeleton skeleton-text" style="width:80%;margin-top:8px"></div>
          <div class="skeleton skeleton-text" style="width:60%;margin-top:6px"></div>
        </div>
      }
    }
  `,
  styles: [`
    :host { display: contents; }
    .stat-card-skeleton { display: flex; align-items: center; gap: 1rem; background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; }
  `]
})
export class SkeletonComponent {
  readonly variant = input<'text' | 'card' | 'table-row' | 'stat-card'>('text');
  readonly columns = input<number[]>([40, 25, 15, 20]);
}
