import { DatePipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';
import { UserProfile } from '../../models/auth-response';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const pw = control.get('newPassword');
  const cp = control.get('confirmPassword');
  return pw && cp && pw.value !== cp.value ? { passwordMismatch: true } : null;
}

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {
  profile: UserProfile | null = null;
  initials = '';
  loading = signal(true);
  profileError = '';
  editMode = signal(false);
  editLoading = false;
  editError = '';
  pwLoading = false;
  pwError = '';
  showCurrentPw = signal(false);
  showNewPw = signal(false);
  showConfirmPw = signal(false);

  editForm;
  passwordForm;

  constructor(
    private auth: AuthService,
    private fb: FormBuilder,
    private toast: ToastService
  ) {
    this.editForm = this.fb.nonNullable.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.nonNullable.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: passwordMatchValidator });
  }

  get ef() { return this.editForm.controls; }
  get pf() { return this.passwordForm.controls; }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.profileError = '';
    this.auth.getProfile().subscribe({
      next: res => {
        this.profile = res.data;
        this.initials = this.getInitials(res.data.name);
        this.editForm.patchValue({ name: res.data.name, email: res.data.email });
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.profileError = err.error?.message || 'Failed to load profile';
        this.toast.error('Could not load profile data.');
      }
    });
  }

  private getInitials(name: string): string {
    return name
      .split(' ')
      .filter(Boolean)
      .map(w => w[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  toggleEdit(): void {
    this.editMode.set(!this.editMode());
    if (this.editMode() && this.profile) {
      this.editForm.patchValue({ name: this.profile.name, email: this.profile.email });
    }
    this.editError = '';
  }

  onEditSubmit(): void {
    if (this.editForm.invalid || this.editLoading) return;

    this.editLoading = true;
    this.editError = '';

    this.auth.updateProfile(this.editForm.getRawValue()).subscribe({
      next: res => {
        this.profile = res.data;
        this.initials = this.getInitials(res.data.name);
        this.editLoading = false;
        this.editMode.set(false);
        this.toast.success('Profile updated successfully.');
      },
      error: (err: HttpErrorResponse) => {
        this.editLoading = false;
        if (err.status === 400 && err.error?.data) {
          const fieldErrors = err.error.data as Record<string, string>;
          this.editError = Object.values(fieldErrors).join('. ');
        } else {
          this.editError = err.error?.message || 'Failed to update profile';
        }
      }
    });
  }

  onPasswordSubmit(): void {
    if (this.passwordForm.invalid || this.pwLoading) return;

    this.pwLoading = true;
    this.pwError = '';

    const { confirmPassword, ...payload } = this.passwordForm.getRawValue();

    this.auth.changePassword(payload).subscribe({
      next: () => {
        this.pwLoading = false;
        this.passwordForm.reset();
        this.toast.success('Password changed successfully.');
      },
      error: (err: HttpErrorResponse) => {
        this.pwLoading = false;
        this.pwError = err.error?.message || 'Failed to change password';
      }
    });
  }
}
