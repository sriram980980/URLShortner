import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ActivatedRoute } from '@angular/router';
import { AnalyticsService } from '../../services/analytics.service';
import { ClickEvent, ClickCount } from '../../models/url.model';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSnackBarModule,
    MatPaginatorModule
  ],
  template: `
    <div class="card-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>URL Analytics</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="form" (ngSubmit)="onLookup()">
            <div style="display: flex; gap: 1rem; align-items: flex-end;">
              <mat-form-field appearance="fill" style="flex: 1;">
                <mat-label>Short Code</mat-label>
                <input
                  matInput
                  formControlName="shortCode"
                  placeholder="e.g., abc123"
                />
                <mat-icon matSuffix>search</mat-icon>
                <mat-error *ngIf="form.get('shortCode')?.hasError('required')">
                  Short code is required
                </mat-error>
              </mat-form-field>
              <button
                mat-raised-button
                color="primary"
                type="submit"
                [disabled]="!form.valid || isLoading"
              >
                <mat-icon *ngIf="!isLoading">search</mat-icon>
                <mat-spinner *ngIf="isLoading" diameter="20"></mat-spinner>
                {{ isLoading ? 'Loading...' : 'Lookup' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card *ngIf="clickCount" class="stats-card">
        <mat-card-header>
          <mat-card-title>Click Statistics</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="count-display">
            {{ clickCount.count }}
            <div class="count-label">Total Clicks</div>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card *ngIf="clickEvents && clickEvents.length > 0" class="events-card">
        <mat-card-header>
          <mat-card-title>Click Events ({{ clickEvents.length }})</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="table-container">
            <table mat-table [dataSource]="clickEvents" class="events-table">
              <!-- Timestamp Column -->
              <ng-container matColumnDef="timestamp">
                <th mat-header-cell *matHeaderCellDef>Timestamp</th>
                <td mat-cell *matCellDef="let element">
                  {{ element.timestamp | date: 'short' }}
                </td>
              </ng-container>

              <!-- IP Address Column -->
              <ng-container matColumnDef="ipAddress">
                <th mat-header-cell *matHeaderCellDef>IP Address</th>
                <td mat-cell *matCellDef="let element">
                  {{ element.ipAddress }}
                </td>
              </ng-container>

              <!-- User Agent Column -->
              <ng-container matColumnDef="userAgent">
                <th mat-header-cell *matHeaderCellDef>User Agent</th>
                <td mat-cell *matCellDef="let element" class="user-agent-cell">
                  {{ element.userAgent }}
                </td>
              </ng-container>

              <!-- URL Column -->
              <ng-container matColumnDef="longUrl">
                <th mat-header-cell *matHeaderCellDef>Original URL</th>
                <td mat-cell *matCellDef="let element" class="url-cell">
                  <a [href]="element.longUrl" target="_blank" rel="noopener noreferrer">
                    {{ element.longUrl | slice: 0: 50 }}{{ element.longUrl.length > 50 ? '...' : '' }}
                  </a>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card *ngIf="currentShortCode && clickEvents?.length === 0 && !isLoading" class="no-data-card">
        <mat-card-content>
          <div class="no-data-message">
            <mat-icon>info</mat-icon>
            <p>No click events found for this short code</p>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card *ngIf="errorMessage" class="error-card">
        <mat-card-content>
          <div class="error-message">
            <mat-icon>error_outline</mat-icon>
            <p>{{ errorMessage }}</p>
          </div>
        </mat-card-content>
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

    .stats-card {
      background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
    }

    .events-card {
      background: #f9f9f9;
    }

    .no-data-card,
    .error-card {
      background: #fff3e0;
    }

    .error-card {
      background: #ffebee;
    }

    .count-display {
      text-align: center;
      padding: 2rem;
    }

    .count-display > div:first-child {
      font-size: 4rem;
      font-weight: bold;
      color: #1976d2;
    }

    .count-label {
      font-size: 1.2rem;
      color: #666;
      margin-top: 0.5rem;
    }

    .table-container {
      overflow-x: auto;
    }

    .events-table {
      width: 100%;
      border-collapse: collapse;
    }

    .events-table th {
      background: #f5f5f5;
      font-weight: 600;
      text-align: left;
      padding: 1rem;
      border-bottom: 2px solid #ddd;
    }

    .events-table td {
      padding: 1rem;
      border-bottom: 1px solid #eee;
    }

    .events-table tr:hover {
      background: #f9f9f9;
    }

    .user-agent-cell,
    .url-cell {
      max-width: 300px;
      word-break: break-word;
      font-size: 0.9rem;
    }

    .url-cell a {
      color: #1976d2;
      text-decoration: none;
    }

    .url-cell a:hover {
      text-decoration: underline;
    }

    .no-data-message,
    .error-message {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1rem;
      padding: 2rem;
      text-align: center;
    }

    .no-data-message mat-icon,
    .error-message mat-icon {
      font-size: 3rem;
      width: 3rem;
      height: 3rem;
      color: #999;
    }

    .error-message mat-icon {
      color: #d32f2f;
    }

    .no-data-message p,
    .error-message p {
      margin: 0;
      font-size: 1.1rem;
      color: #666;
    }

    .error-message p {
      color: #c62828;
    }

    mat-spinner {
      display: inline-block;
    }
  `]
})
export class AnalyticsComponent implements OnInit {
  form!: FormGroup;
  isLoading = false;
  clickCount: ClickCount | null = null;
  clickEvents: ClickEvent[] = [];
  currentShortCode: string | null = null;
  errorMessage: string | null = null;
  displayedColumns: string[] = ['timestamp', 'ipAddress', 'userAgent', 'longUrl'];

  private fb = inject(FormBuilder);
  private analyticsService = inject(AnalyticsService);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);

  ngOnInit(): void {
    this.initializeForm();

    // Check for shortCode in query params
    this.route.queryParams.subscribe(params => {
      if (params['shortCode']) {
        this.form.patchValue({ shortCode: params['shortCode'] });
        this.onLookup();
      }
    });
  }

  private initializeForm(): void {
    this.form = this.fb.group({
      shortCode: ['', [Validators.required]]
    });
  }

  onLookup(): void {
    if (!this.form.valid) {
      return;
    }

    const shortCode = this.form.value.shortCode.trim();
    this.currentShortCode = shortCode;
    this.isLoading = true;
    this.errorMessage = null;
    this.clickCount = null;
    this.clickEvents = [];

    // Fetch both count and events in parallel
    Promise.all([
      this.fetchClickCount(shortCode),
      this.fetchClickEvents(shortCode)
    ]).finally(() => {
      this.isLoading = false;
    });
  }

  private fetchClickCount(shortCode: string): Promise<void> {
    return new Promise((resolve) => {
      this.analyticsService.getClickCount(shortCode).subscribe({
        next: (response: ClickCount) => {
          this.clickCount = response;
          resolve();
        },
        error: (error) => {
          console.error('Error fetching click count:', error);
          this.errorMessage = 'Failed to fetch click count';
          resolve();
        }
      });
    });
  }

  private fetchClickEvents(shortCode: string): Promise<void> {
    return new Promise((resolve) => {
      this.analyticsService.getClickEvents(shortCode).subscribe({
        next: (events: ClickEvent[]) => {
          this.clickEvents = events.sort((a, b) => {
            return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
          });
          resolve();
        },
        error: (error) => {
          console.error('Error fetching click events:', error);
          this.errorMessage = 'Failed to fetch click events';
          resolve();
        }
      });
    });
  }
}
