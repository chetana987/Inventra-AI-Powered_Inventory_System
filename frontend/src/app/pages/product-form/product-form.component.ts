import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ProductService } from '../../services/product.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-product-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.scss'
})
export class ProductFormComponent implements OnInit {
  form;
  isEdit = false;
  loading = false;
  fetching = true;
  error = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private toast: ToastService
  ) {
    this.form = this.fb.nonNullable.group({
      productCode: ['', [Validators.required, Validators.maxLength(50)]],
      name: ['', [Validators.required, Validators.maxLength(150)]],
      category: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(5000)]],
      price: [0, [Validators.required, Validators.min(0.01)]],
      quantity: [0, [Validators.required, Validators.min(0)]],
      minimumStockLevel: [0, [Validators.min(0)]],
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.productService.getById(+id).subscribe({
        next: res => {
          const p = res.data;
          this.form.patchValue({
            productCode: p.productCode,
            name: p.name,
            category: p.category,
            description: p.description,
            price: p.price,
            quantity: p.quantity,
            minimumStockLevel: p.minimumStockLevel,
          });
          this.fetching = false;
        },
        error: () => {
          this.error = 'Failed to load product.';
          this.fetching = false;
        }
      });
    } else {
      this.fetching = false;
    }
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading || this.fetching) return;

    this.loading = true;
    this.error = '';
    const data = this.form.getRawValue();
    const id = this.route.snapshot.paramMap.get('id');

    const request = id
      ? this.productService.update(+id, data)
      : this.productService.create(data);

    request.subscribe({
      next: () => {
        this.toast.success(this.isEdit ? 'Product updated successfully.' : 'Product created successfully.');
        this.router.navigate(['/dashboard/products']);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const body = err.error;
        if (body?.data && typeof body.data === 'object' && err.status === 400) {
          for (const [field, msg] of Object.entries(body.data as Record<string, string>)) {
            const control: AbstractControl | null = this.form.get(field);
            if (control) control.setErrors({ server: msg });
          }
        }
        this.error = (err.status === 403) ? 'Access denied. Admin privileges required.'
                  : body?.message || 'Operation failed. Please try again.';
      }
    });
  }
}
