import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Dashboard } from '../models/dashboard.model';
import { Department } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private http: HttpClient) {}

  getDashboard(country?: string): Observable<Dashboard> {
    let params = new HttpParams();
    if (country) {
      params = params.set('country', country);
    }
    return this.http.get<Dashboard>(`${environment.apiUrl}/dashboard`, { params });
  }

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(`${environment.apiUrl}/departments`);
  }

  getCountries(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/dashboard/countries`);
  }
}
