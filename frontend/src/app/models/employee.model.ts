export interface Employee {
  id: number;
  employeeId: string;
  firstName: string;
  lastName: string;
  email: string;
  designation: string;
  departmentName: string;
  country: string;
  currency: string;
  joinDate: string;
  status: string;
  currentSalary: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  email: string;
  designation: string;
  departmentId: number;
  country: string;
  // No currency field: the currency of record is derived server-side from the country.
  joinDate: string;
  baseSalary: number;
  bonus?: number;
  deductions?: number;
}

export interface UpdateEmployeeRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  designation?: string;
  departmentId?: number;
  status?: string;
}

export interface AuditLog {
  id: number;
  employeeId: number;
  action: string;
  details: string;
  performedBy: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
