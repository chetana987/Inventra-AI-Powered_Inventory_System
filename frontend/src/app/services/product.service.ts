import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth-response';
import { ProductRequest, ProductResponse, PageResponse, ProductListParams, LowStockProduct } from '../models/product';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  list(params: ProductListParams): Observable<ApiResponse<PageResponse<ProductResponse>>> {
    const query: any = {};
    if (params.page != null) query['page'] = params.page;
    if (params.size != null) query['size'] = params.size;
    if (params.sortBy) query['sortBy'] = params.sortBy;
    if (params.sortDir) query['sortDir'] = params.sortDir;
    if (params.name) query['name'] = params.name;
    if (params.category) query['category'] = params.category;
    return this.http.get<ApiResponse<PageResponse<ProductResponse>>>(this.apiUrl, { params: query });
  }

  getById(id: number): Observable<ApiResponse<ProductResponse>> {
    return this.http.get<ApiResponse<ProductResponse>>(`${this.apiUrl}/${id}`);
  }

  create(data: ProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.http.post<ApiResponse<ProductResponse>>(this.apiUrl, data);
  }

  update(id: number, data: ProductRequest): Observable<ApiResponse<ProductResponse>> {
    return this.http.put<ApiResponse<ProductResponse>>(`${this.apiUrl}/${id}`, data);
  }

  lowStock(): Observable<ApiResponse<LowStockProduct[]>> {
    return this.http.get<ApiResponse<LowStockProduct[]>>(`${this.apiUrl}/low-stock`);
  }

  delete(id: number): Observable<ApiResponse<null>> {
    return this.http.delete<ApiResponse<null>>(`${this.apiUrl}/${id}`);
  }
}
