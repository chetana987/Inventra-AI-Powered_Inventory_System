import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { InventoryService } from '../../services/inventory.service';
import { ProductService } from '../../services/product.service';
import { ProductResponse } from '../../models/product';
import { TransactionType } from '../../models/enums';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-stock-move',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './stock-move.component.html',
  styleUrl: './stock-move.component.scss'
})
export class StockMoveComponent implements OnInit {
  form;
  type: TransactionType = 'STOCK_IN';
  loading = false;
  products: ProductResponse[] = [];
  productsLoading = true;
  error = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private inventory: InventoryService,
    private productService: ProductService,
    private toast: ToastService
  ) {
    this.form = this.fb.nonNullable.group({
      productId: [0, [Validators.required, Validators.min(1)]],
      quantity: [0, [Validators.required, Validators.min(1)]],
      remarks: ['']
    });
  }

  ngOnInit(): void {
    this.type = this.route.snapshot.url[0]?.path === 'stock-out' ? 'STOCK_OUT' : 'STOCK_IN';
    this.productService.list({ page: 0, size: 1000 }).subscribe({
      next: res => { this.products = res.data.content; this.productsLoading = false; },
      error: () => { this.productsLoading = false; }
    });
  }

  get title(): string {
    return this.type === 'STOCK_IN' ? 'Stock In' : 'Stock Out';
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;

    this.loading = true;
    this.error = '';
    const data = this.form.getRawValue();
    const request = this.type === 'STOCK_IN'
      ? this.inventory.stockIn(data)
      : this.inventory.stockOut(data);

    request.subscribe({
      next: () => {
        this.toast.success(this.type === 'STOCK_IN' ? 'Stock in recorded successfully.' : 'Stock out recorded successfully.');
        this.router.navigate(['/dashboard/inventory']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.error = err.error?.message || 'Operation failed.';
      }
    });
  }
}
