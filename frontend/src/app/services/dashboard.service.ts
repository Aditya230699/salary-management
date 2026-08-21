import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, shareReplay, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Dashboard } from '../models/dashboard.model';
import { Department } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  // Reference data (countries, departments) changes rarely, but it is needed by the
  // dashboard, the employee list, and the edit dialog. Sharing one replayed stream
  // stops each screen from refetching it; the cache is dropped on error so a failed
  // load is retried instead of being replayed forever.
  private countries$?: Observable<string[]>;
  private departments$?: Observable<Department[]>;

  constructor(private http: HttpClient) {}

  getDashboard(country?: string): Observable<Dashboard> {
    let params = new HttpParams();
    if (country) {
      params = params.set('country', country);
    }
    return this.http.get<Dashboard>(`${environment.apiUrl}/dashboard`, { params });
  }

  getDepartments(): Observable<Department[]> {
    if (!this.departments$) {
      this.departments$ = this.http.get<Department[]>(`${environment.apiUrl}/departments`).pipe(
        catchError(err => {
          this.departments$ = undefined;
          return throwError(() => err);
        }),
        shareReplay(1)
      );
    }
    return this.departments$;
  }

  getCountries(): Observable<string[]> {
    if (!this.countries$) {
      this.countries$ = this.http.get<string[]>(`${environment.apiUrl}/dashboard/countries`).pipe(
        catchError(err => {
          this.countries$ = undefined;
          return throwError(() => err);
        }),
        shareReplay(1)
      );
    }
    return this.countries$;
  }
}
