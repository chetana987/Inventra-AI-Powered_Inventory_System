import { Component, OnInit } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { DashboardService } from '../../services/dashboard.service';
import { ToastService } from '../../services/toast.service';
import { DashboardData } from '../../models/dashboard';
import { SkeletonComponent } from '../../components/skeleton/skeleton';

@Component({
  selector: 'app-dashboard',
  imports: [DatePipe, DecimalPipe, RouterLink, SkeletonComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  data: DashboardData | null = null;
  user: { name?: string; email: string; role: string } | null = null;
  loading = true;
  error = '';

  donutColors = ['#7c3aed', '#06b6d4', '#f59e0b', '#ef4444', '#10b981', '#3b82f6', '#ec4899'];

  constructor(
    private dashboard: DashboardService,
    private auth: AuthService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.user = this.auth.getUser();
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = '';
    this.dashboard.getDashboard().subscribe({
      next: res => { this.data = res.data; this.loading = false; },
      error: () => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
        this.toast.error('Could not load dashboard. Please try again.');
      }
    });
  }

  userName(): string {
    return this.user?.name ? ', ' + this.user.name : '';
  }

  getInitials(): string {
    const name = this.user?.name || this.user?.email || 'U';
    return name.split(' ').map(s => s[0]).join('').slice(0, 2).toUpperCase();
  }

  getTotalTransactions(): number {
    if (!this.data?.monthlyTransactions) return 0;
    return this.data.monthlyTransactions.reduce((s, m) => s + m.inCount + m.outCount, 0);
  }

  getMaxChartValue(points: { inQuantity: number; outQuantity: number }[]): number {
    return Math.max(...points.flatMap(p => [p.inQuantity, p.outQuantity]), 1);
  }

  getMaxCountValue(points: { inCount: number; outCount: number }[]): number {
    return Math.max(...points.flatMap(p => [p.inCount, p.outCount]), 1);
  }

  donutConicGradient(): string {
    if (!this.data?.inventoryDistribution?.length) return '';
    const total = this.data.inventoryDistribution.reduce((s, d) => s + d.count, 0);
    if (total === 0) return '';
    let current = 0;
    const stops = this.data.inventoryDistribution.map((d, i) => {
      const pct = (d.count / total) * 360;
      const start = current;
      const end = current + pct;
      current = end;
      return `${this.donutColors[i % this.donutColors.length]} ${start}deg ${end}deg`;
    });
    return `conic-gradient(${stops.join(', ')})`;
  }
}
