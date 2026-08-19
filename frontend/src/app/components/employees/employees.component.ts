import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { EmployeeService } from '../../services/employee.service';
import { DashboardService } from '../../services/dashboard.service';
import { Employee, PageResponse } from '../../models/employee.model';
import { Department } from '../../models/auth.model';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatPaginatorModule, MatSortModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatProgressSpinnerModule, MatCardModule
  ],
  template: `
    <div class="container">
      <div class="page-header">
        <h2>Employees</h2>
      </div>

      <mat-card class="filter-card">
        <div class="filter-row">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label>Search employees</mat-label>
            <input matInput [(ngModel)]="searchTerm" (ngModelChange)="onSearchChange($event)"
                   placeholder="Name, email, or employee ID">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Department</mat-label>
            <mat-select [(ngModel)]="selectedDepartment" (selectionChange)="loadEmployees()">
              <mat-option value="">All</mat-option>
              <mat-option *ngFor="let dept of departments" [value]="dept.name">{{ dept.name }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Country</mat-label>
            <mat-select [(ngModel)]="selectedCountry" (selectionChange)="loadEmployees()">
              <mat-option value="">All</mat-option>
              <mat-option *ngFor="let c of countries" [value]="c">{{ c }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="selectedStatus" (selectionChange)="loadEmployees()">
              <mat-option value="">All</mat-option>
              <mat-option value="ACTIVE">Active</mat-option>
              <mat-option value="INACTIVE">Inactive</mat-option>
              <mat-option value="ON_LEAVE">On Leave</mat-option>
            </mat-select>
          </mat-form-field>

          <button mat-icon-button (click)="clearFilters()" matTooltip="Clear filters">
            <mat-icon>clear</mat-icon>
          </button>
        </div>
      </mat-card>

      <div *ngIf="loading" class="loading">
        <mat-spinner></mat-spinner>
      </div>

      <mat-card *ngIf="!loading">
        <table mat-table [dataSource]="employees" matSort (matSortChange)="onSort($event)" class="full-width">
          <ng-container matColumnDef="employeeId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let emp">{{ emp.employeeId }}</td>
          </ng-container>

          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="firstName">Name</th>
            <td mat-cell *matCellDef="let emp">{{ emp.firstName }} {{ emp.lastName }}</td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let emp">{{ emp.email }}</td>
          </ng-container>

          <ng-container matColumnDef="department">
            <th mat-header-cell *matHeaderCellDef mat-sort-header="department">Department</th>
            <td mat-cell *matCellDef="let emp">{{ emp.departmentName }}</td>
          </ng-container>

          <ng-container matColumnDef="country">
            <th mat-header-cell *matHeaderCellDef>Country</th>
            <td mat-cell *matCellDef="let emp">{{ emp.country }}</td>
          </ng-container>

          <ng-container matColumnDef="designation">
            <th mat-header-cell *matHeaderCellDef>Designation</th>
            <td mat-cell *matCellDef="let emp">{{ emp.designation }}</td>
          </ng-container>

          <ng-container matColumnDef="salary">
            <th mat-header-cell *matHeaderCellDef>Salary</th>
            <td mat-cell *matCellDef="let emp">
              {{ emp.currentSalary ? (emp.currentSalary | number:'1.0-0') : 'N/A' }}
              <span class="currency">{{ emp.currency }}</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let emp">
              <span class="status-chip" [class]="'status-' + emp.status.toLowerCase()">
                {{ emp.status }}
              </span>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"
              (click)="viewEmployee(row)" class="clickable-row"></tr>
        </table>

        <mat-paginator [length]="totalElements" [pageSize]="pageSize" [pageIndex]="currentPage"
                       [pageSizeOptions]="[10, 20, 50, 100]" (page)="onPageChange($event)"
                       showFirstLastButtons>
        </mat-paginator>
      </mat-card>
    </div>
  `,
  styles: [`
    .filter-card { margin-bottom: 16px; }
    .filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
    .search-field { flex: 1; min-width: 250px; }
    .loading { display: flex; justify-content: center; padding: 48px; }
    .clickable-row { cursor: pointer; }
    .clickable-row:hover { background-color: #f5f5f5; }
    .currency { color: #666; font-size: 0.8rem; margin-left: 4px; }
    .status-chip {
      padding: 4px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: 500;
    }
    .status-active { background: #e8f5e9; color: #2e7d32; }
    .status-inactive { background: #fbe9e7; color: #c62828; }
    .status-on_leave { background: #fff3e0; color: #e65100; }
  `]
})
export class EmployeesComponent implements OnInit {
  employees: Employee[] = [];
  departments: Department[] = [];
  countries: string[] = [];
  displayedColumns = ['employeeId', 'name', 'email', 'department', 'country', 'designation', 'salary', 'status'];

  loading = true;
  totalElements = 0;
  currentPage = 0;
  pageSize = 20;
  sortBy = 'id';
  sortDir = 'asc';

  searchTerm = '';
  selectedDepartment = '';
  selectedCountry = '';
  selectedStatus = '';

  private searchSubject = new Subject<string>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private employeeService: EmployeeService,
    private dashboardService: DashboardService,
    private router: Router
  ) {
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => this.loadEmployees());
  }

  ngOnInit(): void {
    this.dashboardService.getDepartments().subscribe(depts => this.departments = depts);
    // Sourced from the backend so the filter cannot drift from the countries the
    // organisation actually operates in.
    this.dashboardService.getCountries().subscribe(countries => this.countries = countries);
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading = true;
    this.employeeService.getEmployees(
      this.currentPage, this.pageSize,
      this.searchTerm || undefined,
      this.selectedDepartment || undefined,
      this.selectedCountry || undefined,
      this.selectedStatus || undefined,
      this.sortBy, this.sortDir
    ).subscribe({
      next: (page) => {
        this.employees = page.content;
        this.totalElements = page.totalElements;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  onSearchChange(value: string): void {
    this.searchSubject.next(value);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEmployees();
  }

  onSort(sort: Sort): void {
    this.sortBy = sort.active || 'id';
    this.sortDir = sort.direction || 'asc';
    this.loadEmployees();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedDepartment = '';
    this.selectedCountry = '';
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadEmployees();
  }

  viewEmployee(employee: Employee): void {
    this.router.navigate(['/employees', employee.id]);
  }
}
