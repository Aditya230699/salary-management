import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { SalaryService } from './salary.service';
import { environment } from '../../environments/environment';

describe('SalaryService', () => {
  let service: SalaryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [SalaryService]
    });
    service = TestBed.inject(SalaryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should fetch current salary', () => {
    const mockSalary = { id: 1, baseSalary: 100000, bonus: 10000, deductions: 5000, netSalary: 105000 };

    service.getCurrentSalary(1).subscribe(salary => {
      expect(salary.baseSalary).toBe(100000);
      expect(salary.netSalary).toBe(105000);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/employees/1/salary/current`);
    expect(req.request.method).toBe('GET');
    req.flush(mockSalary);
  });

  it('should fetch salary history', () => {
    const mockHistory = [
      { id: 1, baseSalary: 100000, effectiveDate: '2023-01-01' },
      { id: 2, baseSalary: 80000, effectiveDate: '2022-01-01' }
    ];

    service.getSalaryHistory(1).subscribe(history => {
      expect(history.length).toBe(2);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/employees/1/salary/history`);
    req.flush(mockHistory);
  });

  it('should update salary', () => {
    const updateReq = { baseSalary: 120000, effectiveDate: '2024-04-01', notes: 'Annual raise' };
    const mockResponse = { id: 3, baseSalary: 120000, netSalary: 120000 };

    service.updateSalary(1, updateReq).subscribe(salary => {
      expect(salary.baseSalary).toBe(120000);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/employees/1/salary`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.baseSalary).toBe(120000);
    req.flush(mockResponse);
  });
});
