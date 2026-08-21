import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ShortenRequest, ShortenResponse } from '../models/url.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UrlService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  shorten(request: ShortenRequest): Observable<ShortenResponse> {
    return this.http.post<ShortenResponse>(`${this.apiUrl}/api/v1/shorten`, request);
  }
}
