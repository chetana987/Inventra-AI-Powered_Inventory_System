import { Component, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InventoryService } from '../../services/inventory.service';
import { ToastService } from '../../services/toast.service';
import { InventoryResponse } from '../../models/inventory';
import { PageResponse } from '../../models/product';
import { SkeletonComponent } from '../../components/skeleton/skeleton';
import { EmptyStateComponent } from '../../components/empty-state/empty-state';

@Component({
  selector: 'app-transactions',
  imports: [DatePipe, FormsModule, RouterLink, SkeletonComponent, EmptyStateComponent],
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss'
})
export class TransactionsComponent implements OnInit {
  pageData: PageResponse<InventoryResponse> | null = null;
  loading = true;
  error = '';
  filterProductId: number | null = null;
  currentPage = 0;

  constructor(
    private inventory: InventoryService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading = true;
    this.error = '';
    this.inventory.getHistory(this.filterProductId ?? undefined, this.currentPage).subscribe({
      next: res => { this.pageData = res.data; this.loading = false; },
      error: () => {
        this.error = 'Failed to load transactions.';
        this.loading = false;
        this.toast.error('Could not load transaction history.');
      }
    });
  }

  search(): void {
    this.currentPage = 0;
    this.loadHistory();
  }

  clearFilter(): void {
    this.filterProductId = null;
    this.currentPage = 0;
    this.loadHistory();
  }

  goToPage(page: number): void {
    if (page < 0 || (this.pageData && page >= this.pageData.totalPages)) return;
    this.currentPage = page;
    this.loadHistory();
  }

  getPageRange(): number[] {
    if (!this.pageData) return [];
    const total = this.pageData.totalPages;
    const current = this.pageData.page;
    const start = Math.max(0, current - 2);
    const end = Math.min(total, start + 5);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }
}
