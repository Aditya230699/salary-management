import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule, RouterModule, MatToolbarModule, MatButtonModule,
    MatIconModule, MatSidenavModule, MatListModule
  ],
  template: `
    <div class="layout-container">
      <mat-toolbar color="primary" class="toolbar" *ngIf="authService.isLoggedIn()">
        <mat-icon>payments</mat-icon>
        <span class="app-title">Salary Management</span>
        <span class="spacer"></span>
        <span class="user-name">{{ authService.getUser()?.fullName }}</span>
        <button mat-icon-button (click)="logout()" aria-label="Logout">
          <mat-icon>logout</mat-icon>
        </button>
      </mat-toolbar>

      <div class="content-wrapper">
        <nav class="sidenav" *ngIf="authService.isLoggedIn()">
          <mat-nav-list>
            <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Dashboard</span>
            </a>
            <a mat-list-item routerLink="/employees" routerLinkActive="active">
              <mat-icon matListItemIcon>people</mat-icon>
              <span matListItemTitle>Employees</span>
            </a>
          </mat-nav-list>
        </nav>

        <main [class.main-content]="authService.isLoggedIn()" [class.full-page]="!authService.isLoggedIn()">
          <ng-content></ng-content>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .layout-container { display: flex; flex-direction: column; min-height: 100vh; }
    .toolbar { position: sticky; top: 0; z-index: 100; }
    .app-title { margin-left: 8px; font-size: 1.1rem; }
    .spacer { flex: 1 1 auto; }
    .user-name { margin-right: 16px; font-size: 0.9rem; }
    .content-wrapper { display: flex; flex: 1; overflow: hidden; }
    .sidenav { width: 220px; border-right: 1px solid #e0e0e0; background: white; }
    .main-content { flex: 1; overflow-y: auto; padding: 24px; }
    .full-page { flex: 1; width: 100%; }
    .active { background-color: rgba(63, 81, 181, 0.08); }
  `]
})
export class LayoutComponent {
  constructor(public authService: AuthService, private router: Router) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
