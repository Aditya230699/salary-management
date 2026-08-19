import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';
import { environment } from '../../environments/environment';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EmployeeService]
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch paginated employees', () => {
    const mockPage = {
      content: [{ id: 1, firstName: 'John', lastName: 'Doe', email: 'john@acme.com' }],
      totalElements: 100,
      totalPages: 5,
      size: 20,
      number: 0,
      first: true,
      last: false
    };

    service.getEmployees(0, 20).subscribe(page => {
      expect(page.content.length).toBe(1);
      expect(page.totalElements).toBe(100);
    });

    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/employees`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    req.flush(mockPage);
  });

  it('should fetch single employee', () => {
    const mockEmployee = { id: 1, firstName: 'John', lastName: 'Doe' };

    service.getEmployee(1).subscribe(emp => {
      expect(emp.firstName).toBe('John');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/employees/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockEmployee);
  });

  it('should include filters in request params', () => {
    service.getEmployees(0, 20, 'john', 'Engineering', 'USA', 'ACTIVE').subscribe();

    const req = httpMock.expectOne(r => r.url === `${environment.apiUrl}/employees`);
    expect(req.request.params.get('search')).toBe('john');
    expect(req.request.params.get('department')).toBe('Engineering');
    expect(req.request.params.get('country')).toBe('USA');
    expect(req.request.params.get('status')).toBe('ACTIVE');
    req.flush({ content: [], totalElements: 0 });
  });
});
