export interface ProductResponse {
  id: number;
  productCode: string;
  name: string;
  category: string;
  description: string;
  price: number;
  quantity: number;
  minimumStockLevel: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProductRequest {
  productCode: string;
  name: string;
  category: string;
  description?: string;
  price: number;
  quantity: number;
  minimumStockLevel?: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface LowStockProduct {
  id: number;
  productCode: string;
  name: string;
  category: string;
  quantity: number;
  minimumStockLevel: number;
}

export interface ProductListParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
  name?: string;
  category?: string;
}
