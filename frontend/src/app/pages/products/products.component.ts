import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { ToastService } from '../../services/toast.service';
import { ProductResponse, PageResponse } from '../../models/product';
import { SkeletonComponent } from '../../components/skeleton/skeleton';
import { EmptyStateComponent } from '../../components/empty-state/empty-state';

@Component({
  selector: 'app-products',
  imports: [FormsModule, RouterLink, CurrencyPipe, SkeletonComponent, EmptyStateComponent],
  templateUrl: './products.component.html',
  styleUrl: './products.component.scss'
})
export class ProductsComponent implements OnInit {
  pageData: PageResponse<ProductResponse> | null = null;
  loading = true;
  error = '';

  searchName = '';
  searchCategory = '';
  currentPage = 0;
  sortBy = 'id';
  sortDir = 'asc';
  size = 10;

  deleteId: number | null = null;

  constructor(
    private productService: ProductService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.error = '';

    this.productService.list({
      page: this.currentPage,
      size: this.size,
      sortBy: this.sortBy,
      sortDir: this.sortDir,
      name: this.searchName || undefined,
      category: this.searchCategory || undefined
    }).subscribe({
      next: res => { this.pageData = res.data; this.loading = false; },
      error: () => { this.error = 'Failed to load products.'; this.loading = false; this.toast.error('Failed to load products.'); }
    });
  }

  search(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  resetSearch(): void {
    this.searchName = '';
    this.searchCategory = '';
    this.currentPage = 0;
    this.loadProducts();
  }

  sort(column: string): void {
    if (this.sortBy === column) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDir = 'asc';
    }
    this.loadProducts();
  }

  sortIcon(column: string): string {
    if (this.sortBy !== column) return '';
    return this.sortDir === 'asc' ? '\u25B2' : '\u25BC';
  }

  goToPage(page: number): void {
    if (page < 0 || (this.pageData && page >= this.pageData.totalPages)) return;
    this.currentPage = page;
    this.loadProducts();
  }

  confirmDelete(id: number): void {
    this.deleteId = id;
  }

  cancelDelete(): void {
    this.deleteId = null;
  }

  getPageRange(data: PageResponse<ProductResponse>): number[] {
    const total = data.totalPages;
    const current = data.page;
    const start = Math.max(0, current - 2);
    const end = Math.min(total, start + 5);
    return Array.from({ length: end - start }, (_, i) => start + i);
  }

  executeDelete(): void {
    if (this.deleteId == null) return;
    this.productService.delete(this.deleteId).subscribe({
      next: () => {
        this.deleteId = null;
        this.toast.success('Product deleted successfully.');
        this.loadProducts();
      },
      error: () => {
        this.toast.error('Failed to delete product.');
        this.deleteId = null;
      }
    });
  }
}
