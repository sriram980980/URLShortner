import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { UrlService } from '../../services/url.service';
import { ShortenResponse } from '../../models/url.model';

@Component({
  selector: 'app-url-shortener',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  template: `
    <div class="card-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Shorten URL</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onSubmit()">
            <mat-form-field class="full-width" appearance="fill">
              <mat-label>URL to Shorten</mat-label>
              <input
                matInput
                formControlName="longUrl"
                type="url"
                placeholder="https://example.com/very/long/url"
              />
              <mat-icon matSuffix>link</mat-icon>
              <mat-error *ngIf="form.get('longUrl')?.hasError('required')">
                URL is required
              </mat-error>
              <mat-error *ngIf="form.get('longUrl')?.hasError('pattern')">
                Please enter a valid URL
              </mat-error>
            </mat-form-field>

            <mat-form-field class="full-width" appearance="fill" style="margin-top: 1rem;">
              <mat-label>TTL (seconds) - Optional</mat-label>
              <input
                matInput
                formControlName="customTtlSeconds"
                type="number"
                placeholder="3600 (1 hour)"
                min="60"
              />
              <mat-icon matSuffix>schedule</mat-icon>
              <mat-hint *ngIf="form.get('customTtlSeconds')?.value">
                TTL: {{ form.get('customTtlSeconds')?.value }} seconds
              </mat-hint>
              <mat-error *ngIf="form.get('customTtlSeconds')?.hasError('min')">
                TTL must be at least 60 seconds
              </mat-error>
            </mat-form-field>

            <div class="button-group" style="margin-top: 2rem;">
              <button
                mat-raised-button
                color="primary"
                type="submit"
                [disabled]="!form.valid || isLoading"
              >
                <mat-icon *ngIf="!isLoading">send</mat-icon>
                <mat-spinner *ngIf="isLoading" diameter="20" style="margin-right: 0.5rem;"></mat-spinner>
                {{ isLoading ? 'Processing...' : 'Shorten URL' }}
              </button>
              <button
                mat-stroked-button
                type="button"
                (click)="onClear()"
                [disabled]="isLoading"
              >
                Clear
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card *ngIf="result" class="result-card">
        <mat-card-header>
          <mat-card-title>Shortened URL Created!</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="result-item">
            <label>Short Code:</label>
            <div class="result-value">
              <span>{{ result.shortCode }}</span>
              <button
                mat-icon-button
                (click)="copyToClipboard(result.shortCode)"
                matTooltip="Copy short code"
              >
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </div>

          <div class="result-item">
            <label>Short URL:</label>
            <div class="result-value">
              <span>{{ result.shortUrl }}</span>
              <button
                mat-icon-button
                (click)="copyToClipboard(result.shortUrl)"
                matTooltip="Copy short URL"
              >
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </div>

          <div class="result-item">
            <label>Original URL:</label>
            <div class="result-value">
              <span class="url-text">{{ result.originalUrl }}</span>
              <button
                mat-icon-button
                (click)="copyToClipboard(result.originalUrl)"
                matTooltip="Copy original URL"
              >
                <mat-icon>content_copy</mat-icon>
              </button>
            </div>
          </div>

          <div class="result-item" *ngIf="result.expiresAt">
            <label>Expires At:</label>
            <div class="result-value">
              <span>{{ result.expiresAt | date: 'medium' }}</span>
            </div>
          </div>
        </mat-card-content>
        <mat-card-actions>
          <button mat-stroked-button (click)="openAnalytics()">
            View Analytics
          </button>
          <button mat-stroked-button (click)="onClear()">
            Create Another
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .card-container {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
    }

    mat-card {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }

    .full-width {
      width: 100%;
    }

    .button-group {
      display: flex;
      gap: 1rem;
    }

    button:disabled {
      cursor: not-allowed;
    }

    .result-card {
      background: linear-gradient(135deg, #e8f5e9 0%, #f1f8e9 100%);
    }

    .result-item {
      margin-bottom: 1.5rem;
    }

    .result-item label {
      display: block;
      font-weight: 500;
      color: #333;
      margin-bottom: 0.5rem;
    }

    .result-value {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      background: white;
      padding: 0.75rem;
      border-radius: 4px;
      border: 1px solid #e0e0e0;
      word-break: break-all;
    }

    .result-value span {
      flex: 1;
      line-height: 1.5;
    }

    .url-text {
      color: #0277bd;
    }

    mat-card-actions {
      display: flex;
      gap: 1rem;
      padding-top: 1rem;
      border-top: 1px solid #e0e0e0;
    }

    mat-spinner {
      display: inline-block;
    }
  `]
})
export class UrlShortenerComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  result: ShortenResponse | null = null;

  private fb = inject(FormBuilder);
  private urlService = inject(UrlService);
  private snackBar = inject(MatSnackBar);

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.form = this.fb.group({
      longUrl: [
        '',
        [
          Validators.required,
          Validators.pattern(/^https?:\/\/.+/)
        ]
      ],
      customTtlSeconds: [
        null,
        [Validators.min(60)]
      ]
    });
  }

  onSubmit(): void {
    if (!this.form.valid) {
      return;
    }

    this.isLoading = true;
    const request = {
      longUrl: this.form.value.longUrl,
      customTtlSeconds: this.form.value.customTtlSeconds || undefined
    };

    this.urlService.shorten(request).subscribe({
      next: (response: ShortenResponse) => {
        this.result = response;
        this.isLoading = false;
        this.snackBar.open('URL shortened successfully!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.isLoading = false;
        const message = error.error?.message || error.message || 'Failed to shorten URL';
        this.snackBar.open(`Error: ${message}`, 'Close', { duration: 5000, panelClass: ['error-snackbar'] });
      }
    });
  }

  onClear(): void {
    this.form.reset();
    this.result = null;
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.snackBar.open('Copied to clipboard!', 'Close', { duration: 2000 });
    }).catch(err => {
      console.error('Failed to copy:', err);
      this.snackBar.open('Failed to copy', 'Close', { duration: 2000 });
    });
  }

  openAnalytics(): void {
    if (this.result) {
      window.location.href = `/analytics?shortCode=${this.result.shortCode}`;
    }
  }
}
