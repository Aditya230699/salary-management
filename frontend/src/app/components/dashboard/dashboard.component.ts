import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { DashboardService } from '../../services/dashboard.service';
import { Dashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, MatIconModule, MatTableModule],
  template: `
    <div class="container">
      <h2>Dashboard</h2>

      <div *ngIf="loading" class="loading">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="dashboard && !loading">
        <div class="stats-grid">
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.totalEmployees | number }}</div>
            <div class="stat-label">Total Employees</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.activeEmployees | number }}</div>
            <div class="stat-label">Active Employees</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.averageSalary | number:'1.0-0' }}</div>
            <div class="stat-label">Average Salary</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.totalPayroll | number:'1.0-0' }}</div>
            <div class="stat-label">Total Payroll</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.minSalary | number:'1.0-0' }}</div>
            <div class="stat-label">Min Salary</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ dashboard.maxSalary | number:'1.0-0' }}</div>
            <div class="stat-label">Max Salary</div>
          </mat-card>
        </div>

        <div class="breakdown-grid">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Employees by Department</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <table mat-table [dataSource]="getDeptData()" class="full-width">
                <ng-container matColumnDef="department">
                  <th mat-header-cell *matHeaderCellDef>Department</th>
                  <td mat-cell *matCellDef="let row">{{ row.name }}</td>
                </ng-container>
                <ng-container matColumnDef="count">
                  <th mat-header-cell *matHeaderCellDef>Count</th>
                  <td mat-cell *matCellDef="let row">{{ row.count | number }}</td>
                </ng-container>
                <ng-container matColumnDef="avgSalary">
                  <th mat-header-cell *matHeaderCellDef>Avg Salary</th>
                  <td mat-cell *matCellDef="let row">{{ row.avgSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="payroll">
                  <th mat-header-cell *matHeaderCellDef>Total Payroll</th>
                  <td mat-cell *matCellDef="let row">{{ row.payroll | number:'1.0-0' }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="['department', 'count', 'avgSalary', 'payroll']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['department', 'count', 'avgSalary', 'payroll']"></tr>
              </table>
            </mat-card-content>
          </mat-card>

          <mat-card>
            <mat-card-header>
              <mat-card-title>Employees by Country</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <table mat-table [dataSource]="getCountryData()" class="full-width">
                <ng-container matColumnDef="country">
                  <th mat-header-cell *matHeaderCellDef>Country</th>
                  <td mat-cell *matCellDef="let row">{{ row.name }}</td>
                </ng-container>
                <ng-container matColumnDef="count">
                  <th mat-header-cell *matHeaderCellDef>Count</th>
                  <td mat-cell *matCellDef="let row">{{ row.count | number }}</td>
                </ng-container>
                <ng-container matColumnDef="avgSalary">
                  <th mat-header-cell *matHeaderCellDef>Avg Salary</th>
                  <td mat-cell *matCellDef="let row">{{ row.avgSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="payroll">
                  <th mat-header-cell *matHeaderCellDef>Total Payroll</th>
                  <td mat-cell *matCellDef="let row">{{ row.payroll | number:'1.0-0' }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="['country', 'count', 'avgSalary', 'payroll']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['country', 'count', 'avgSalary', 'payroll']"></tr>
              </table>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .loading { display: flex; justify-content: center; padding: 48px; }
    .breakdown-grid {
      display: grid; grid-template-columns: 1fr 1fr; gap: 16px;
    }
    @media (max-width: 960px) {
      .breakdown-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;
  loading = true;

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.dashboard = data;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  getDeptData(): any[] {
    if (!this.dashboard) return [];
    return Object.keys(this.dashboard.employeesByDepartment).map(name => ({
      name,
      count: this.dashboard!.employeesByDepartment[name],
      avgSalary: this.dashboard!.avgSalaryByDepartment[name] || 0,
      payroll: this.dashboard!.payrollByDepartment[name] || 0
    }));
  }

  getCountryData(): any[] {
    if (!this.dashboard) return [];
    return Object.keys(this.dashboard.employeesByCountry).map(name => ({
      name,
      count: this.dashboard!.employeesByCountry[name],
      avgSalary: this.dashboard!.avgSalaryByCountry[name] || 0,
      payroll: this.dashboard!.payrollByCountry[name] || 0
    }));
  }
}
