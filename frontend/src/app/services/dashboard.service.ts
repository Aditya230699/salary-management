import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../environments/environment';
import { Dashboard } from '../models/dashboard.model';
import { Department } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  /**
   * Reference data changes rarely, so the lists are fetched once per session and
   * replayed to later subscribers instead of refetched on every screen.
   */
  private departments$?: Observable<Department[]>;
  private countries$?: Observable<string[]>;

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
      this.departments$ = this.http
        .get<Department[]>(`${environment.apiUrl}/departments`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
    }
    return this.departments$;
  }

  /** Countries come from the backend so the UI does not hardcode where the org operates. */
  getCountries(): Observable<string[]> {
    if (!this.countries$) {
      this.countries$ = this.http
        .get<string[]>(`${environment.apiUrl}/dashboard/countries`)
        .pipe(shareReplay({ bufferSize: 1, refCount: false }));
    }
    return this.countries$;
  }
}
