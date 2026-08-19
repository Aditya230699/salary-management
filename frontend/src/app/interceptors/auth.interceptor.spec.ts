import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpStatusCode, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        AuthService,
        { provide: Router, useValue: router }
      ]
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('attaches the bearer token when one is stored', () => {
    localStorage.setItem('auth_token', 'stored-token');

    http.get('/api/employees').subscribe();

    const req = httpMock.expectOne('/api/employees');
    expect(req.request.headers.get('Authorization')).toBe('Bearer stored-token');
    req.flush({});
  });

  it('sends no Authorization header when there is no token', () => {
    http.get('/api/employees').subscribe();

    const req = httpMock.expectOne('/api/employees');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('clears the session and redirects to login on 401', () => {
    localStorage.setItem('auth_token', 'expired-token');

    http.get('/api/employees').subscribe({ error: () => undefined });

    httpMock.expectOne('/api/employees')
      .flush({ message: 'Unauthorized' }, { status: HttpStatusCode.Unauthorized, statusText: 'Unauthorized' });

    expect(authService.isLoggedIn()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { reason: 'session-expired' } });
  });

  it('does not redirect when the login request itself returns 401', () => {
    // A wrong password must be reported on the login screen, not trigger a redirect loop.
    http.post('/api/auth/login', { username: 'x', password: 'y' }).subscribe({ error: () => undefined });

    httpMock.expectOne('/api/auth/login')
      .flush({ message: 'Invalid username or password' },
             { status: HttpStatusCode.Unauthorized, statusText: 'Unauthorized' });

    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('passes other errors through without touching the session', () => {
    localStorage.setItem('auth_token', 'valid-token');
    let capturedStatus = 0;

    http.get('/api/employees').subscribe({ error: (err) => capturedStatus = err.status });

    httpMock.expectOne('/api/employees')
      .flush({}, { status: HttpStatusCode.InternalServerError, statusText: 'Server Error' });

    expect(capturedStatus).toBe(HttpStatusCode.InternalServerError);
    expect(authService.isLoggedIn()).toBeTrue();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
