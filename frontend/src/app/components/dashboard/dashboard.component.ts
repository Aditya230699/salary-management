import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DashboardService } from '../../services/dashboard.service';
import { Dashboard, SalaryStats } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatProgressSpinnerModule, MatIconModule,
    MatTableModule, MatFormFieldModule, MatSelectModule, MatTooltipModule
  ],
  template: `
    <div class="container">
      <div class="page-header">
        <h2>Dashboard</h2>
        <mat-form-field appearance="outline" class="country-filter">
          <mat-label>Country</mat-label>
          <mat-select [(ngModel)]="selectedCountry" (selectionChange)="load()">
            <mat-option [value]="''">All countries</mat-option>
            <mat-option *ngFor="let c of countries" [value]="c">{{ c }}</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div *ngIf="loading" class="loading">
        <mat-spinner></mat-spinner>
      </div>

      <div class="error-message" *ngIf="errorMessage">{{ errorMessage }}</div>

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
            <div class="stat-value">{{ countryCount | number }}</div>
            <div class="stat-label">Countries</div>
          </mat-card>
          <mat-card class="stat-card">
            <div class="stat-value">{{ departmentCount | number }}</div>
            <div class="stat-label">Departments</div>
          </mat-card>
        </div>

        <mat-card class="explainer">
          <mat-icon>info</mat-icon>
          <span *ngIf="!dashboard.filteredCountry">
            Pay is shown per country because salaries are held in local currency.
            Averaging across currencies would produce a meaningless figure. Pick a
            country to compare departments and designations like for like.
          </span>
          <span *ngIf="dashboard.filteredCountry">
            Showing {{ dashboard.filteredCountry }} only, so all figures below are in
            a single currency and directly comparable.
          </span>
        </mat-card>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Pay by country</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="dashboard.salaryStatsByCountry" class="full-width">
              <ng-container matColumnDef="group">
                <th mat-header-cell *matHeaderCellDef>Country</th>
                <td mat-cell *matCellDef="let row">{{ row.group }}</td>
              </ng-container>
              <ng-container matColumnDef="employeeCount">
                <th mat-header-cell *matHeaderCellDef>Employees</th>
                <td mat-cell *matCellDef="let row">{{ row.employeeCount | number }}</td>
              </ng-container>
              <ng-container matColumnDef="minSalary">
                <th mat-header-cell *matHeaderCellDef>Min</th>
                <td mat-cell *matCellDef="let row">{{ row.minSalary | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="percentile25">
                <th mat-header-cell *matHeaderCellDef matTooltip="25% of employees earn below this">P25</th>
                <td mat-cell *matCellDef="let row">{{ row.percentile25 | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="medianSalary">
                <th mat-header-cell *matHeaderCellDef matTooltip="Half earn below, half above">Median</th>
                <td mat-cell *matCellDef="let row"><strong>{{ row.medianSalary | number:'1.0-0' }}</strong></td>
              </ng-container>
              <ng-container matColumnDef="percentile75">
                <th mat-header-cell *matHeaderCellDef matTooltip="75% of employees earn below this">P75</th>
                <td mat-cell *matCellDef="let row">{{ row.percentile75 | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="maxSalary">
                <th mat-header-cell *matHeaderCellDef>Max</th>
                <td mat-cell *matCellDef="let row">{{ row.maxSalary | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="averageSalary">
                <th mat-header-cell *matHeaderCellDef>Average</th>
                <td mat-cell *matCellDef="let row">{{ row.averageSalary | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="totalPayroll">
                <th mat-header-cell *matHeaderCellDef>Total payroll</th>
                <td mat-cell *matCellDef="let row">{{ row.totalPayroll | number:'1.0-0' }}</td>
              </ng-container>
              <ng-container matColumnDef="currency">
                <th mat-header-cell *matHeaderCellDef>Currency</th>
                <td mat-cell *matCellDef="let row"><span class="currency-badge">{{ row.currency }}</span></td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="statsColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: statsColumns"></tr>
            </table>
          </mat-card-content>
        </mat-card>

        <div class="breakdown-grid" *ngIf="dashboard.filteredCountry">
          <mat-card>
            <mat-card-header>
              <mat-card-title>Pay by department</mat-card-title>
              <mat-card-subtitle>{{ dashboard.filteredCountry }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <table mat-table [dataSource]="dashboard.salaryStatsByDepartment || []" class="full-width">
                <ng-container matColumnDef="group">
                  <th mat-header-cell *matHeaderCellDef>Department</th>
                  <td mat-cell *matCellDef="let row">{{ row.group }}</td>
                </ng-container>
                <ng-container matColumnDef="employeeCount">
                  <th mat-header-cell *matHeaderCellDef>Employees</th>
                  <td mat-cell *matCellDef="let row">{{ row.employeeCount | number }}</td>
                </ng-container>
                <ng-container matColumnDef="medianSalary">
                  <th mat-header-cell *matHeaderCellDef>Median</th>
                  <td mat-cell *matCellDef="let row">{{ row.medianSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="averageSalary">
                  <th mat-header-cell *matHeaderCellDef>Average</th>
                  <td mat-cell *matCellDef="let row">{{ row.averageSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="totalPayroll">
                  <th mat-header-cell *matHeaderCellDef>Payroll</th>
                  <td mat-cell *matCellDef="let row">{{ row.totalPayroll | number:'1.0-0' }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="groupColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: groupColumns"></tr>
              </table>
            </mat-card-content>
          </mat-card>

          <mat-card>
            <mat-card-header>
              <mat-card-title>Pay by designation</mat-card-title>
              <mat-card-subtitle>{{ dashboard.filteredCountry }}</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <table mat-table [dataSource]="dashboard.salaryStatsByDesignation || []" class="full-width">
                <ng-container matColumnDef="group">
                  <th mat-header-cell *matHeaderCellDef>Designation</th>
                  <td mat-cell *matCellDef="let row">{{ row.group }}</td>
                </ng-container>
                <ng-container matColumnDef="employeeCount">
                  <th mat-header-cell *matHeaderCellDef>Employees</th>
                  <td mat-cell *matCellDef="let row">{{ row.employeeCount | number }}</td>
                </ng-container>
                <ng-container matColumnDef="medianSalary">
                  <th mat-header-cell *matHeaderCellDef>Median</th>
                  <td mat-cell *matCellDef="let row">{{ row.medianSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="averageSalary">
                  <th mat-header-cell *matHeaderCellDef>Average</th>
                  <td mat-cell *matCellDef="let row">{{ row.averageSalary | number:'1.0-0' }}</td>
                </ng-container>
                <ng-container matColumnDef="totalPayroll">
                  <th mat-header-cell *matHeaderCellDef>Payroll</th>
                  <td mat-cell *matCellDef="let row">{{ row.totalPayroll | number:'1.0-0' }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="groupColumns"></tr>
                <tr mat-row *matRowDef="let row; columns: groupColumns"></tr>
              </table>
            </mat-card-content>
          </mat-card>
        </div>

        <mat-card>
          <mat-card-header>
            <mat-card-title>Headcount by department</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="departmentHeadcount" class="full-width">
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef>Department</th>
                <td mat-cell *matCellDef="let row">{{ row.name }}</td>
              </ng-container>
              <ng-container matColumnDef="count">
                <th mat-header-cell *matHeaderCellDef>Employees</th>
                <td mat-cell *matCellDef="let row">{{ row.count | number }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="['name', 'count']"></tr>
              <tr mat-row *matRowDef="let row; columns: ['name', 'count']"></tr>
            </table>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .loading { display: flex; justify-content: center; padding: 48px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
    .country-filter { width: 220px; }
    .breakdown-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
    .explainer {
      display: flex; align-items: center; gap: 12px; padding: 12px 16px;
      background: #eef2ff; color: #3730a3; font-size: 0.9rem;
    }
    .currency-badge {
      background: #eef2ff; color: #3730a3; padding: 2px 8px;
      border-radius: 10px; font-size: 0.75rem; font-weight: 600;
    }
    @media (max-width: 1100px) { .breakdown-grid { grid-template-columns: 1fr; } }
  `]
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;
  countries: string[] = [];
  selectedCountry = '';
  loading = true;
  errorMessage = '';

  /**
   * Derived views are computed once per load. Binding a method call straight into
   * `[dataSource]` rebuilt the array on every change detection pass, which forced the
   * table to re-render continuously.
   */
  departmentHeadcount: Array<{ name: string; count: number }> = [];
  countryCount = 0;
  departmentCount = 0;

  statsColumns = ['group', 'employeeCount', 'minSalary', 'percentile25', 'medianSalary',
                  'percentile75', 'maxSalary', 'averageSalary', 'totalPayroll', 'currency'];
  groupColumns = ['group', 'employeeCount', 'medianSalary', 'averageSalary', 'totalPayroll'];

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.getCountries().subscribe({
      next: (countries) => this.countries = countries,
      error: () => this.countries = []
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getDashboard(this.selectedCountry || undefined).subscribe({
      next: (data) => {
        this.dashboard = data;
        this.departmentHeadcount = Object.keys(data.employeesByDepartment)
          .map(name => ({ name, count: data.employeesByDepartment[name] }))
          .sort((a, b) => b.count - a.count);
        this.countryCount = Object.keys(data.employeesByCountry).length;
        this.departmentCount = this.departmentHeadcount.length;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load the dashboard. Please try again.';
        this.loading = false;
      }
    });
  }
}
