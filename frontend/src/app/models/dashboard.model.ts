export interface Dashboard {
  totalEmployees: number;
  activeEmployees: number;
  averageSalary: number;
  totalPayroll: number;
  minSalary: number;
  maxSalary: number;
  employeesByDepartment: { [key: string]: number };
  employeesByCountry: { [key: string]: number };
  avgSalaryByDepartment: { [key: string]: number };
  avgSalaryByCountry: { [key: string]: number };
  payrollByDepartment: { [key: string]: number };
  payrollByCountry: { [key: string]: number };
}
