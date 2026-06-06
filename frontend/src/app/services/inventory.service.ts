import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth-response';
import { PageResponse } from '../models/product';
import { InventoryRequest, InventoryResponse } from '../models/inventory';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private readonly apiUrl = `${environment.apiUrl}/inventory`;

  constructor(private http: HttpClient) {}

  stockIn(data: InventoryRequest): Observable<ApiResponse<InventoryResponse>> {
    return this.http.post<ApiResponse<InventoryResponse>>(`${this.apiUrl}/stock-in`, data);
  }

  stockOut(data: InventoryRequest): Observable<ApiResponse<InventoryResponse>> {
    return this.http.post<ApiResponse<InventoryResponse>>(`${this.apiUrl}/stock-out`, data);
  }

  getHistory(productId?: number, page = 0, size = 10): Observable<ApiResponse<PageResponse<InventoryResponse>>> {
    const params: any = { page, size };
    if (productId) params['productId'] = productId;
    return this.http.get<ApiResponse<PageResponse<InventoryResponse>>>(`${this.apiUrl}/history`, { params });
  }
}
