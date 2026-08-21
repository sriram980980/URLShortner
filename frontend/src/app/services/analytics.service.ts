import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClickEvent, ClickCount } from '../models/url.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getClickEvents(shortCode: string): Observable<ClickEvent[]> {
    return this.http.get<ClickEvent[]>(`${this.apiUrl}/api/v1/analytics/clicks/${shortCode}`);
  }

  getClickCount(shortCode: string): Observable<ClickCount> {
    return this.http.get<ClickCount>(`${this.apiUrl}/api/v1/analytics/clicks/${shortCode}/count`);
  }
}
