import { Component, computed, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('password');
  const cp = control.get('confirmPassword');
  return pw && cp && pw.value !== cp.value ? { passwordMismatch: true } : null;
}

function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  const v: string = control.value || '';
  const errors: Record<string, boolean> = {};
  if (!/[A-Z]/.test(v)) errors['uppercase'] = true;
  if (!/[a-z]/.test(v)) errors['lowercase'] = true;
  if (!/[0-9]/.test(v)) errors['number'] = true;
  if (!/[^A-Za-z0-9]/.test(v)) errors['special'] = true;
  return Object.keys(errors).length ? errors : null;
}

@Component({
  selector: 'app-signup',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss'
})
export class SignupComponent {
  signupForm;
  errorMessage = '';
  successMessage = '';
  loading = false;
  showPassword = signal(false);
  showConfirm = signal(false);

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.nonNullable.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8), passwordStrengthValidator]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: passwordMatchValidator });
  }

  passwordStrength = computed(() => {
    const v = this.signupForm.controls.password.value || '';
    let score = 0;
    if (v.length >= 8) score++;
    if (v.length >= 12) score++;
    if (/[A-Z]/.test(v)) score++;
    if (/[a-z]/.test(v)) score++;
    if (/[0-9]/.test(v)) score++;
    if (/[^A-Za-z0-9]/.test(v)) score++;
    if (score <= 2) return { label: 'Weak', class: 'weak', pct: 25 };
    if (score <= 3) return { label: 'Fair', class: 'fair', pct: 50 };
    if (score <= 5) return { label: 'Strong', class: 'strong', pct: 75 };
    return { label: 'Very Strong', class: 'very-strong', pct: 100 };
  });

  get f() { return this.signupForm.controls; }

  onSubmit(): void {
    if (this.signupForm.invalid || this.loading) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const { confirmPassword, ...payload } = this.signupForm.getRawValue();

    this.auth.register(payload).subscribe({
      next: () => {
        this.successMessage = 'Account created successfully! Redirecting to login...';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
      }
    });
  }
}
