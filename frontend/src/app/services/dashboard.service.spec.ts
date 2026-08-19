import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { environment } from '../../environments/environment';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), DashboardService]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests the dashboard without a country filter by default', () => {
    service.getDashboard().subscribe();

    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/dashboard`);
    expect(req.request.params.has('country')).toBeFalse();
    req.flush({ totalEmployees: 0 });
  });

  it('passes the country filter through when supplied', () => {
    service.getDashboard('India').subscribe();

    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/dashboard`);
    expect(req.request.params.get('country')).toBe('India');
    req.flush({ totalEmployees: 0, filteredCountry: 'India' });
  });

  it('fetches the supported countries from the backend', () => {
    service.getCountries().subscribe(countries => {
      expect(countries).toEqual(['India', 'USA']);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/dashboard/countries`);
    expect(req.request.method).toBe('GET');
    req.flush(['India', 'USA']);
  });

  it('caches reference data so repeated screens do not refetch it', () => {
    service.getCountries().subscribe();
    httpMock.expectOne(`${environment.apiUrl}/dashboard/countries`).flush(['India']);

    service.getCountries().subscribe(countries => expect(countries).toEqual(['India']));

    // No second request is issued; verify() in afterEach would fail on an outstanding one.
    httpMock.expectNone(`${environment.apiUrl}/dashboard/countries`);
  });

  it('caches the department list', () => {
    service.getDepartments().subscribe();
    httpMock.expectOne(`${environment.apiUrl}/departments`).flush([{ id: 1, name: 'Engineering', description: '' }]);

    service.getDepartments().subscribe(depts => expect(depts.length).toBe(1));

    httpMock.expectNone(`${environment.apiUrl}/departments`);
  });
});
