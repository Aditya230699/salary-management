import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-icon mat-card-avatar class="login-icon">payments</mat-icon>
          <mat-card-title>Salary Management System</mat-card-title>
          <mat-card-subtitle>Sign in to continue</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form (ngSubmit)="onLogin()" #loginForm="ngForm">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Username</mat-label>
              <input matInput [(ngModel)]="username" name="username" required
                     placeholder="Enter username" autocomplete="username">
              <mat-icon matSuffix>person</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Password</mat-label>
              <input matInput [(ngModel)]="password" name="password" required
                     [type]="hidePassword ? 'password' : 'text'"
                     placeholder="Enter password" autocomplete="current-password">
              <button mat-icon-button matSuffix (click)="hidePassword = !hidePassword" type="button">
                <mat-icon>{{hidePassword ? 'visibility_off' : 'visibility'}}</mat-icon>
              </button>
            </mat-form-field>

            <div class="error-message" *ngIf="errorMessage">{{ errorMessage }}</div>

            <button mat-raised-button color="primary" class="full-width login-btn"
                    type="submit" [disabled]="loading || !username || !password">
              <mat-spinner diameter="20" *ngIf="loading"></mat-spinner>
              <span *ngIf="!loading">Sign In</span>
            </button>
          </form>

          <div class="demo-credentials">
            <p><strong>Demo Credentials:</strong></p>
            <p>Username: <code>hr_manager</code> | Password: <code>password123</code></p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex; justify-content: center; align-items: center;
      min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    .login-card { width: 400px; padding: 24px; }
    .login-icon { font-size: 40px; width: 40px; height: 40px; color: #3f51b5; }
    .full-width { width: 100%; }
    .login-btn { height: 48px; margin-top: 16px; font-size: 16px; }
    .error-message { color: #f44336; margin-bottom: 8px; text-align: center; }
    .demo-credentials {
      margin-top: 24px; padding: 12px; background: #f5f5f5;
      border-radius: 4px; font-size: 0.85rem;
    }
    .demo-credentials code { background: #e0e0e0; padding: 2px 6px; border-radius: 3px; }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  hidePassword = true;
  loading = false;
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
    }
  }

  onLogin(): void {
    this.loading = true;
    this.errorMessage = '';

    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.status === 401 ? 'Invalid username or password' : 'Login failed. Please try again.';
      }
    });
  }
}
