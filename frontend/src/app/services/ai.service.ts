import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse } from '../models/auth-response';
import { AiQueryRequest, AiQueryResponse } from '../models/ai';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly apiUrl = `${environment.apiUrl}/ai`;

  constructor(private http: HttpClient) {}

  query(question: string): Observable<ApiResponse<AiQueryResponse>> {
    return this.http.post<ApiResponse<AiQueryResponse>>(`${this.apiUrl}/query`, { question } as AiQueryRequest);
  }
}
