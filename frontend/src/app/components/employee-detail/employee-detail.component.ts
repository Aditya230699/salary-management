import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { EmployeeService } from '../../services/employee.service';
import { SalaryService } from '../../services/salary.service';
import { Employee } from '../../models/employee.model';
import { Salary } from '../../models/salary.model';
import { SalaryDialogComponent } from '../salary-dialog/salary-dialog.component';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatTabsModule,
    MatTableModule, MatDialogModule, MatChipsModule, MatProgressSpinnerModule, MatSnackBarModule
  ],
  template: `
    <div class="container" *ngIf="employee; else loadingTpl">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h2>{{ employee.firstName }} {{ employee.lastName }}</h2>
        <span class="status-chip" [class]="'status-' + employee.status.toLowerCase()">
          {{ employee.status }}
        </span>
      </div>

      <mat-tab-group>
        <mat-tab label="Details">
          <div class="detail-grid">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Personal Information</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="detail-row">
                  <span class="label">Employee ID</span>
                  <span class="value">{{ employee.employeeId }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Email</span>
                  <span class="value">{{ employee.email }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Designation</span>
                  <span class="value">{{ employee.designation }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Department</span>
                  <span class="value">{{ employee.departmentName }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Country</span>
                  <span class="value">{{ employee.country }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">Join Date</span>
                  <span class="value">{{ employee.joinDate }}</span>
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card>
              <mat-card-header>
                <mat-card-title>Current Compensation</mat-card-title>
                <button mat-raised-button color="primary" (click)="openSalaryDialog()">
                  <mat-icon>edit</mat-icon> Update Salary
                </button>
              </mat-card-header>
              <mat-card-content>
                <div *ngIf="currentSalary">
                  <div class="detail-row">
                    <span class="label">Base Salary</span>
                    <span class="value salary-value">{{ currentSalary.baseSalary | number:'1.0-2' }} {{ currentSalary.currency }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Bonus</span>
                    <span class="value">{{ currentSalary.bonus | number:'1.0-2' }} {{ currentSalary.currency }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Deductions</span>
                    <span class="value">{{ currentSalary.deductions | number:'1.0-2' }} {{ currentSalary.currency }}</span>
                  </div>
                  <div class="detail-row net-salary">
                    <span class="label">Net Salary</span>
                    <span class="value salary-value">{{ currentSalary.netSalary | number:'1.0-2' }} {{ currentSalary.currency }}</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Effective Since</span>
                    <span class="value">{{ currentSalary.effectiveDate }}</span>
                  </div>
                </div>
                <div *ngIf="!currentSalary" class="no-data">No salary data available</div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <mat-tab label="Salary History">
          <mat-card>
            <table mat-table [dataSource]="salaryHistory" class="full-width" *ngIf="salaryHistory.length > 0">
              <ng-container matColumnDef="effectiveDate">
                <th mat-header-cell *matHeaderCellDef>Effective Date</th>
                <td mat-cell *matCellDef="let s">{{ s.effectiveDate }}</td>
              </ng-container>
              <ng-container matColumnDef="endDate">
                <th mat-header-cell *matHeaderCellDef>End Date</th>
                <td mat-cell *matCellDef="let s">{{ s.endDate || 'Current' }}</td>
              </ng-container>
              <ng-container matColumnDef="baseSalary">
                <th mat-header-cell *matHeaderCellDef>Base Salary</th>
                <td mat-cell *matCellDef="let s">{{ s.baseSalary | number:'1.0-2' }}</td>
              </ng-container>
              <ng-container matColumnDef="bonus">
                <th mat-header-cell *matHeaderCellDef>Bonus</th>
                <td mat-cell *matCellDef="let s">{{ s.bonus | number:'1.0-2' }}</td>
              </ng-container>
              <ng-container matColumnDef="netSalary">
                <th mat-header-cell *matHeaderCellDef>Net Salary</th>
                <td mat-cell *matCellDef="let s">{{ s.netSalary | number:'1.0-2' }}</td>
              </ng-container>
              <ng-container matColumnDef="notes">
                <th mat-header-cell *matHeaderCellDef>Notes</th>
                <td mat-cell *matCellDef="let s">{{ s.notes || '-' }}</td>
              </ng-container>
              <tr mat-header-row *matHeaderRowDef="salaryColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: salaryColumns"></tr>
            </table>
            <div *ngIf="salaryHistory.length === 0" class="no-data">No salary history</div>
          </mat-card>
        </mat-tab>
      </mat-tab-group>
    </div>

    <ng-template #loadingTpl>
      <div class="loading"><mat-spinner></mat-spinner></div>
    </ng-template>
  `,
  styles: [`
    .page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; }
    .detail-row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #eee; }
    .label { color: #666; font-weight: 500; }
    .value { font-weight: 400; }
    .salary-value { font-size: 1.1rem; font-weight: 600; color: #3f51b5; }
    .net-salary { background: #f3f4ff; padding: 12px; margin: 8px -16px; border-radius: 4px; }
    .no-data { text-align: center; padding: 32px; color: #999; }
    .loading { display: flex; justify-content: center; padding: 48px; }
    .status-chip { padding: 4px 12px; border-radius: 12px; font-size: 0.8rem; font-weight: 500; }
    .status-active { background: #e8f5e9; color: #2e7d32; }
    .status-inactive { background: #fbe9e7; color: #c62828; }
    .status-on_leave { background: #fff3e0; color: #e65100; }
    mat-card-header { display: flex; align-items: center; justify-content: space-between; }
    @media (max-width: 960px) { .detail-grid { grid-template-columns: 1fr; } }
  `]
})
export class EmployeeDetailComponent implements OnInit {
  employee: Employee | null = null;
  currentSalary: Salary | null = null;
  salaryHistory: Salary[] = [];
  salaryColumns = ['effectiveDate', 'endDate', 'baseSalary', 'bonus', 'netSalary', 'notes'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private employeeService: EmployeeService,
    private salaryService: SalaryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadEmployee(id);
    this.loadSalary(id);
  }

  loadEmployee(id: number): void {
    this.employeeService.getEmployee(id).subscribe(emp => this.employee = emp);
  }

  loadSalary(id: number): void {
    this.salaryService.getCurrentSalary(id).subscribe({
      next: (salary) => this.currentSalary = salary,
      error: () => this.currentSalary = null
    });
    this.salaryService.getSalaryHistory(id).subscribe({
      next: (history) => this.salaryHistory = history,
      error: () => this.salaryHistory = []
    });
  }

  openSalaryDialog(): void {
    const dialogRef = this.dialog.open(SalaryDialogComponent, {
      width: '500px',
      data: { employee: this.employee, currentSalary: this.currentSalary }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.salaryService.updateSalary(this.employee!.id, result).subscribe({
          next: () => {
            this.snackBar.open('Salary updated successfully', 'Close', { duration: 3000 });
            this.loadSalary(this.employee!.id);
          },
          error: () => {
            this.snackBar.open('Failed to update salary', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/employees']);
  }
}
