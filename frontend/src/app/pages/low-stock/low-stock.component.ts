import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { ToastService } from '../../services/toast.service';
import { LowStockProduct } from '../../models/product';
import { SkeletonComponent } from '../../components/skeleton/skeleton';
import { EmptyStateComponent } from '../../components/empty-state/empty-state';

@Component({
  selector: 'app-low-stock',
  imports: [FormsModule, SkeletonComponent, EmptyStateComponent],
  templateUrl: './low-stock.component.html',
  styleUrl: './low-stock.component.scss'
})
export class LowStockComponent implements OnInit {
  allProducts: LowStockProduct[] = [];
  filtered: LowStockProduct[] = [];
  loading = true;
  error = '';

  searchQuery = '';
  selectedCategory = '';

  constructor(
    private productService: ProductService,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.productService.lowStock().subscribe({
      next: res => {
        this.allProducts = res.data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load low stock products.';
        this.loading = false;
        this.toast.error('Could not load low stock data.');
      }
    });
  }

  get categories(): string[] {
    return [...new Set(this.allProducts.map(p => p.category))].sort();
  }

  applyFilters(): void {
    let list = this.allProducts;
    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      list = list.filter(p =>
        p.name.toLowerCase().includes(q) || p.productCode.toLowerCase().includes(q)
      );
    }
    if (this.selectedCategory) {
      list = list.filter(p => p.category === this.selectedCategory);
    }
    this.filtered = list;
  }

  refresh(): void {
    this.toast.info('Refreshing data…');
    this.load();
  }
}
