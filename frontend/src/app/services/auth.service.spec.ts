import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should login and store token', () => {
    const mockResponse = {
      token: 'test-jwt-token',
      username: 'hr_manager',
      fullName: 'HR Manager',
      role: 'HR_MANAGER'
    };

    service.login({ username: 'hr_manager', password: 'password123' }).subscribe(response => {
      expect(response.token).toBe('test-jwt-token');
      expect(service.isLoggedIn()).toBeTrue();
      expect(service.getToken()).toBe('test-jwt-token');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should logout and clear storage', () => {
    localStorage.setItem('auth_token', 'test-token');
    localStorage.setItem('auth_user', '{}');

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.getToken()).toBeNull();
  });

  it('should return user info', () => {
    const user = { token: 'tok', username: 'hr', fullName: 'HR Manager', role: 'HR_MANAGER' };
    localStorage.setItem('auth_user', JSON.stringify(user));

    expect(service.getUser()?.fullName).toBe('HR Manager');
  });
});
