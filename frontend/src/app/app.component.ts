import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatToolbarModule, MatButtonModule],
  template: `
    <mat-toolbar color="primary">
      <span>URL Shortener</span>
      <span style="flex: 1 1 auto;"></span>
      <a mat-button routerLink="/shorten" routerLinkActive="active">Shorten</a>
      <a mat-button routerLink="/analytics" routerLinkActive="active">Analytics</a>
    </mat-toolbar>
    <main style="padding: 2rem; max-width: 900px; margin: auto;">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    :host {
      display: block;
    }
    main {
      min-height: calc(100vh - 64px);
    }
  `]
})
export class AppComponent {
  title = 'URL Shortener';
}
