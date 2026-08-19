/**
 * Pay distribution for one comparable group. Every money field is expressed in
 * `currency`; the backend never mixes currencies inside a single group.
 */
export interface SalaryStats {
  group: string;
  currency: string;
  employeeCount: number;
  minSalary: number;
  percentile25: number;
  medianSalary: number;
  percentile75: number;
  maxSalary: number;
  averageSalary: number;
  totalPayroll: number;
}

export interface Dashboard {
  totalEmployees: number;
  activeEmployees: number;
  employeesByDepartment: { [key: string]: number };
  employeesByCountry: { [key: string]: number };
  salaryStatsByCountry: SalaryStats[];
  /** Set only when the caller narrowed to one country. */
  filteredCountry: string | null;
  salaryStatsByDepartment: SalaryStats[] | null;
  salaryStatsByDesignation: SalaryStats[] | null;
}
