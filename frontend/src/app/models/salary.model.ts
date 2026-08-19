export interface Salary {
  id: number;
  employeeId: number;
  employeeName: string;
  baseSalary: number;
  bonus: number;
  deductions: number;
  netSalary: number;
  currency: string;
  effectiveDate: string;
  endDate: string | null;
  notes: string;
  createdBy: string;
  createdAt: string;
}

export interface UpdateSalaryRequest {
  baseSalary: number;
  bonus?: number;
  deductions?: number;
  effectiveDate: string;
  notes?: string;
}
