import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Employee } from '../../models/employee.model';
import { Salary, UpdateSalaryRequest } from '../../models/salary.model';

@Component({
  selector: 'app-salary-dialog',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatDatepickerModule, MatNativeDateModule
  ],
  template: `
    <h2 mat-dialog-title>Update Salary</h2>
    <p class="employee-name">{{ data.employee.firstName }} {{ data.employee.lastName }} ({{ data.employee.currency }})</p>

    <mat-dialog-content>
      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Base Salary</mat-label>
        <input matInput type="number" [(ngModel)]="baseSalary" required min="1">
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Bonus</mat-label>
        <input matInput type="number" [(ngModel)]="bonus" min="0">
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Deductions</mat-label>
        <input matInput type="number" [(ngModel)]="deductions" min="0">
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Effective Date</mat-label>
        <input matInput [matDatepicker]="picker" [(ngModel)]="effectiveDate" required>
        <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
        <mat-datepicker #picker></mat-datepicker>
      </mat-form-field>

      <mat-form-field appearance="outline" class="full-width">
        <mat-label>Notes</mat-label>
        <textarea matInput [(ngModel)]="notes" rows="3" placeholder="Reason for salary change"></textarea>
      </mat-form-field>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSubmit()"
              [disabled]="!baseSalary || !effectiveDate">
        Update Salary
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; }
    .employee-name { color: #666; margin: -8px 0 16px 24px; }
    mat-dialog-content { min-width: 400px; }
  `]
})
export class SalaryDialogComponent {
  baseSalary: number;
  bonus: number;
  deductions: number;
  effectiveDate: Date;
  notes = '';

  constructor(
    public dialogRef: MatDialogRef<SalaryDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { employee: Employee; currentSalary: Salary | null }
  ) {
    this.baseSalary = data.currentSalary?.baseSalary || 0;
    this.bonus = data.currentSalary?.bonus || 0;
    this.deductions = data.currentSalary?.deductions || 0;
    this.effectiveDate = new Date();
  }

  onSubmit(): void {
    const result: UpdateSalaryRequest = {
      baseSalary: this.baseSalary,
      bonus: this.bonus,
      deductions: this.deductions,
      effectiveDate: this.formatDate(this.effectiveDate),
      notes: this.notes || undefined
    };
    this.dialogRef.close(result);
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}
