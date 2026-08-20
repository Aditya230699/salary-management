import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Employee, UpdateEmployeeRequest } from '../../models/employee.model';
import { Department } from '../../models/auth.model';
import { DashboardService } from '../../services/dashboard.service';

/**
 * Edits the employee attributes an HR manager owns, including status, which is how an
 * employee is deactivated. Pay is deliberately not editable here: salary changes go
 * through the salary dialog so that every change lands in the dated history.
 */
@Component({
  selector: 'app-employee-dialog',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>Edit employee</h2>
    <p class="employee-id">{{ data.employee.employeeId }}</p>

    <mat-dialog-content>
      <div class="row">
        <mat-form-field appearance="outline">
          <mat-label>First name</mat-label>
          <input matInput [(ngModel)]="form.firstName" required maxlength="100">
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Last name</mat-label>
          <input matInput [(ngModel)]="form.lastName" required maxlength="100">
        </mat-form-field>
      </div>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Email</mat-label>
        <input matInput type="email" [(ngModel)]="form.email" required>
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Designation</mat-label>
        <input matInput [(ngModel)]="form.designation" required>
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Department</mat-label>
        <mat-select [(ngModel)]="form.departmentId">
          <mat-option *ngFor="let d of departments" [value]="d.id">{{ d.name }}</mat-option>
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="form.status">
          <mat-option value="ACTIVE">Active</mat-option>
          <mat-option value="ON_LEAVE">On leave</mat-option>
          <mat-option value="INACTIVE">Inactive</mat-option>
        </mat-select>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close(null)">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="!isValid()">
        Save changes
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; }
    .row { display: flex; gap: 12px; }
    .row mat-form-field { flex: 1; }
    .employee-id { color: #666; margin: -8px 0 16px 24px; font-size: 0.9rem; }
    mat-dialog-content { min-width: 440px; }
  `]
})
export class EmployeeDialogComponent implements OnInit {
  departments: Department[] = [];
  form: UpdateEmployeeRequest & { firstName: string; lastName: string; email: string } ;

  constructor(
    public dialogRef: MatDialogRef<EmployeeDialogComponent>,
    private dashboardService: DashboardService,
    @Inject(MAT_DIALOG_DATA) public data: { employee: Employee }
  ) {
    this.form = {
      firstName: data.employee.firstName,
      lastName: data.employee.lastName,
      email: data.employee.email,
      designation: data.employee.designation,
      status: data.employee.status
    };
  }

  ngOnInit(): void {
    this.dashboardService.getDepartments().subscribe(departments => {
      this.departments = departments;
      // Match on name because the list DTO carries the department name, not its id.
      this.form.departmentId = departments.find(d => d.name === this.data.employee.departmentName)?.id;
    });
  }

  isValid(): boolean {
    return !!this.form.firstName?.trim()
        && !!this.form.lastName?.trim()
        && !!this.form.email?.trim()
        && !!this.form.designation?.trim();
  }

  onSubmit(): void {
    this.dialogRef.close(this.form);
  }
}
