import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(c => c.LoginComponent) },
  { path: 'signup', loadComponent: () => import('./pages/signup/signup.component').then(c => c.SignupComponent) },
  { path: '', loadComponent: () => import('./pages/landing/landing.component').then(c => c.LandingComponent) },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/layout/layout.component').then(c => c.LayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./pages/dashboard/dashboard.component').then(c => c.DashboardComponent) },
      { path: 'products', loadComponent: () => import('./pages/products/products.component').then(c => c.ProductsComponent) },
      { path: 'products/new', loadComponent: () => import('./pages/product-form/product-form.component').then(c => c.ProductFormComponent) },
      { path: 'products/:id/edit', loadComponent: () => import('./pages/product-form/product-form.component').then(c => c.ProductFormComponent) },
      { path: 'inventory', loadComponent: () => import('./pages/transactions/transactions.component').then(c => c.TransactionsComponent) },
      { path: 'inventory/stock-in', loadComponent: () => import('./pages/stock-move/stock-move.component').then(c => c.StockMoveComponent) },
      { path: 'inventory/stock-out', loadComponent: () => import('./pages/stock-move/stock-move.component').then(c => c.StockMoveComponent) },
      { path: 'low-stock', loadComponent: () => import('./pages/low-stock/low-stock.component').then(c => c.LowStockComponent) },
      { path: 'profile', loadComponent: () => import('./pages/profile/profile.component').then(c => c.ProfileComponent) },
    ]
  },
  { path: 'ai', canActivate: [authGuard], loadComponent: () => import('./pages/ai-assistant/ai-assistant.component').then(c => c.AiAssistantComponent) },
  { path: '**', redirectTo: '' }
];
