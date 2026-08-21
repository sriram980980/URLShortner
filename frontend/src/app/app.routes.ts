import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'shorten', pathMatch: 'full' },
  {
    path: 'shorten',
    loadComponent: () => import('./components/url-shortener/url-shortener.component').then(m => m.UrlShortenerComponent)
  },
  {
    path: 'analytics',
    loadComponent: () => import('./components/analytics/analytics.component').then(m => m.AnalyticsComponent)
  },
  { path: '**', redirectTo: 'shorten' }
];
