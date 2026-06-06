export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface LoginData {
  token: string;
  email: string;
  role: string;
  name: string;
}

export interface RegisterData {
  token: string;
  email: string;
  role: string;
  name: string;
}

export interface ProfileUpdateRequest {
  name: string;
  email: string;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserProfile {
  id: number;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
  timestamp: string;
}
