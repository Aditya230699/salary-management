import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonModule } from '@angular/material/button';
import { DashboardService } from '../../services/dashboard.service';
import { Dashboard } from '../../models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatProgressSpinnerModule, MatIconModule,
    MatTableModule, MatFormFieldModule, MatSelectModule, MatTooltipModule, MatButtonModule
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
        <mat-spinner diameter="48"></mat-spinner>
        <p class="loading-text">Loading salary analytics...</p>
      </div>

      <mat-card class="error-card" *ngIf="errorMessage && !loading">
        <mat-card-content>
          <mat-icon color="warn">error_outline</mat-icon>
          <span>{{ errorMessage }}</span>
          <button mat-flat-button color="primary" (click)="load()">Retry</button>
        </mat-card-content>
      </mat-card>

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

        <mat-card class="table-card">
          <mat-card-header>
            <mat-card-title>Pay by country</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <table mat-table [dataSource]="dashboard.salaryStatsByCountry || []" class="full-width">
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

        <mat-card class="table-card" *ngIf="departmentHeadcount.length > 0">
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
    .container { max-width: 1400px; margin: 0 auto; }
    .loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 64px 0; }
    .loading-text { margin-top: 16px; color: #666; font-size: 0.95rem; }
    .error-card { margin-bottom: 24px; border-left: 4px solid #f44336; }
    .error-card mat-card-content { display: flex; align-items: center; gap: 12px; }
    .page-header { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 24px; }
    .country-filter { width: 220px; }
    .stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
    .stat-card { text-align: center; padding: 20px; }
    .stat-value { font-size: 2rem; font-weight: 700; color: #3f51b5; }
    .stat-label { color: #666; font-size: 0.85rem; margin-top: 4px; text-transform: uppercase; letter-spacing: 0.5px; }
    .table-card { margin-bottom: 24px; }
    .breakdown-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
    .explainer {
      display: flex; align-items: center; gap: 12px; padding: 12px 16px;
      background: #eef2ff; color: #3730a3; font-size: 0.9rem; margin-bottom: 16px;
    }
    .currency-badge {
      background: #eef2ff; color: #3730a3; padding: 2px 8px;
      border-radius: 10px; font-size: 0.75rem; font-weight: 600;
    }
    .full-width { width: 100%; }
    @media (max-width: 1100px) { .breakdown-grid { grid-template-columns: 1fr; } }
    @media (max-width: 768px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
  `]
})
export class DashboardComponent implements OnInit {
  dashboard: Dashboard | null = null;
  countries: string[] = [];
  selectedCountry = '';
  loading = true;
  errorMessage = '';

  departmentHeadcount: Array<{ name: string; count: number }> = [];
  countryCount = 0;
  departmentCount = 0;

  statsColumns = ['group', 'employeeCount', 'minSalary', 'percentile25', 'medianSalary',
                  'percentile75', 'maxSalary', 'averageSalary', 'totalPayroll', 'currency'];
  groupColumns = ['group', 'employeeCount', 'medianSalary', 'averageSalary', 'totalPayroll'];

  constructor(
    private dashboardService: DashboardService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.dashboardService.getCountries().subscribe({
      next: (countries) => {
        this.countries = countries || [];
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error('Failed to load countries:', err);
        this.countries = [];
        this.cdr.markForCheck();
      }
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.cdr.markForCheck();

    this.dashboardService.getDashboard(this.selectedCountry || undefined).subscribe({
      next: (data) => {
        try {
          this.dashboard = data;
          if (data && data.employeesByDepartment) {
            this.departmentHeadcount = Object.keys(data.employeesByDepartment)
              .map(name => ({ name, count: data.employeesByDepartment[name] }))
              .sort((a, b) => b.count - a.count);
          } else {
            this.departmentHeadcount = [];
          }

          if (data && data.employeesByCountry) {
            this.countryCount = Object.keys(data.employeesByCountry).length;
          } else {
            this.countryCount = 0;
          }

          this.departmentCount = this.departmentHeadcount.length;
        } catch (e) {
          console.error('Error processing dashboard response:', e);
          this.errorMessage = 'Failed to process dashboard data.';
        } finally {
          this.loading = false;
          this.cdr.markForCheck();
        }
      },
      error: (err) => {
        console.error('Dashboard request error:', err);
        this.errorMessage = 'Could not load dashboard data from server. Please try again.';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }
}
