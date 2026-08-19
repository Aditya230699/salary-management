import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Salary, UpdateSalaryRequest } from '../models/salary.model';

@Injectable({
  providedIn: 'root'
})
export class SalaryService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getCurrentSalary(employeeId: number): Observable<Salary> {
    return this.http.get<Salary>(`${this.apiUrl}/employees/${employeeId}/salary/current`);
  }

  getSalaryHistory(employeeId: number): Observable<Salary[]> {
    return this.http.get<Salary[]>(`${this.apiUrl}/employees/${employeeId}/salary/history`);
  }

  updateSalary(employeeId: number, request: UpdateSalaryRequest): Observable<Salary> {
    return this.http.put<Salary>(`${this.apiUrl}/employees/${employeeId}/salary`, request);
  }
}
