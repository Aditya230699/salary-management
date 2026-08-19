package com.salarymanagement.seed;

import com.salarymanagement.entity.*;
import com.salarymanagement.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final SalaryRepository salaryRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int TOTAL_EMPLOYEES = 10000;

    private static final String[] FIRST_NAMES = {
        "James", "Mary", "Robert", "Patricia", "John", "Jennifer", "Michael", "Linda",
        "David", "Elizabeth", "William", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
        "Thomas", "Sarah", "Charles", "Karen", "Daniel", "Lisa", "Matthew", "Nancy",
        "Anthony", "Betty", "Mark", "Margaret", "Donald", "Sandra", "Steven", "Ashley",
        "Andrew", "Kimberly", "Paul", "Emily", "Joshua", "Donna", "Kenneth", "Michelle",
        "Raj", "Priya", "Amit", "Sunita", "Vikram", "Neha", "Arjun", "Pooja",
        "Hans", "Anna", "Klaus", "Petra", "Friedrich", "Ingrid", "Wolfgang", "Helga",
        "Oliver", "Charlotte", "Harry", "Amelia", "George", "Isla", "Jack", "Sophie",
        "Liam", "Olivia", "Noah", "Emma", "Ethan", "Ava", "Lucas", "Mia"
    };

    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
        "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
        "Sharma", "Patel", "Kumar", "Singh", "Gupta", "Verma", "Reddy", "Nair",
        "Mueller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker",
        "Thompson", "Evans", "Walker", "Wright", "Roberts", "Green", "Baker", "Hall",
        "Mitchell", "Campbell", "Parker", "Edwards", "Collins", "Stewart", "Morris", "Murphy"
    };

    private static final String[] DESIGNATIONS = {
        "Software Engineer", "Senior Software Engineer", "Staff Engineer",
        "Engineering Manager", "Product Manager", "Senior Product Manager",
        "Data Analyst", "Senior Data Analyst", "Data Scientist",
        "UX Designer", "Senior UX Designer", "Design Lead",
        "QA Engineer", "Senior QA Engineer", "QA Lead",
        "DevOps Engineer", "Senior DevOps Engineer", "Cloud Architect",
        "Business Analyst", "Technical Writer", "Scrum Master",
        "VP Engineering", "CTO", "Director of Engineering"
    };

    private static final Map<String, String> COUNTRY_CURRENCIES = Map.of(
        "India", "INR",
        "USA", "USD",
        "UK", "GBP",
        "Germany", "EUR",
        "Australia", "AUD"
    );

    // Base salary ranges per country (in local currency)
    private static final Map<String, double[]> SALARY_RANGES = Map.of(
        "India", new double[]{400000, 5000000},
        "USA", new double[]{60000, 250000},
        "UK", new double[]{35000, 150000},
        "Germany", new double[]{45000, 180000},
        "Australia", new double[]{70000, 220000}
    );

    private static final String[] DEPARTMENTS = {
        "Engineering", "Product", "Design", "Quality Assurance",
        "Data Science", "DevOps", "Human Resources", "Finance",
        "Marketing", "Sales"
    };

    public DataSeeder(EmployeeRepository employeeRepository,
                      DepartmentRepository departmentRepository,
                      SalaryRepository salaryRepository,
                      AppUserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            System.out.println("Data already seeded. Skipping...");
            return;
        }

        System.out.println("Seeding data...");
        long startTime = System.currentTimeMillis();

        seedUsers();
        List<Department> departments = seedDepartments();
        seedEmployees(departments);

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Data seeding completed in %d ms. Total employees: %d%n", duration, TOTAL_EMPLOYEES);
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("hr_manager")) {
            AppUser hrManager = AppUser.builder()
                    .username("hr_manager")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("HR Manager")
                    .role(AppUser.Role.HR_MANAGER)
                    .enabled(true)
                    .build();
            userRepository.save(hrManager);
        }

        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = AppUser.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Admin")
                    .role(AppUser.Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }

    private List<Department> seedDepartments() {
        List<Department> departments = new ArrayList<>();
        for (String deptName : DEPARTMENTS) {
            Department dept = departmentRepository.findByName(deptName)
                    .orElseGet(() -> departmentRepository.save(
                            Department.builder().name(deptName).description(deptName + " Department").build()));
            departments.add(dept);
        }
        return departments;
    }

    private void seedEmployees(List<Department> departments) {
        Random random = new Random(42); // Fixed seed for reproducibility
        Set<String> usedEmails = new HashSet<>();
        List<String> countries = new ArrayList<>(COUNTRY_CURRENCIES.keySet());

        List<Employee> employeeBatch = new ArrayList<>();
        List<Salary> salaryBatch = new ArrayList<>();

        for (int i = 0; i < TOTAL_EMPLOYEES; i++) {
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

            // Ensure unique email
            String email;
            int emailAttempt = 0;
            do {
                email = firstName.toLowerCase() + "." + lastName.toLowerCase()
                        + (emailAttempt > 0 ? emailAttempt : "") + "@acme.com";
                emailAttempt++;
            } while (usedEmails.contains(email));
            usedEmails.add(email);

            String country = countries.get(random.nextInt(countries.size()));
            String currency = COUNTRY_CURRENCIES.get(country);
            Department department = departments.get(random.nextInt(departments.size()));
            String designation = DESIGNATIONS[random.nextInt(DESIGNATIONS.length)];

            // Random join date between 2015 and 2024
            LocalDate joinDate = LocalDate.of(
                    2015 + random.nextInt(10),
                    1 + random.nextInt(12),
                    1 + random.nextInt(28));

            Employee.EmployeeStatus status = random.nextInt(100) < 90
                    ? Employee.EmployeeStatus.ACTIVE
                    : (random.nextBoolean() ? Employee.EmployeeStatus.INACTIVE : Employee.EmployeeStatus.ON_LEAVE);

            Employee employee = Employee.builder()
                    .employeeId(String.format("EMP-%05d", i + 1))
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .designation(designation)
                    .department(department)
                    .country(country)
                    .currency(currency)
                    .joinDate(joinDate)
                    .status(status)
                    .build();

            employeeBatch.add(employee);

            if (employeeBatch.size() >= 500) {
                List<Employee> saved = employeeRepository.saveAll(employeeBatch);
                createSalariesForBatch(saved, random, salaryBatch);
                salaryRepository.saveAll(salaryBatch);
                employeeBatch.clear();
                salaryBatch.clear();

                if ((i + 1) % 2000 == 0) {
                    System.out.printf("  Seeded %d / %d employees...%n", i + 1, TOTAL_EMPLOYEES);
                }
            }
        }

        // Save remaining
        if (!employeeBatch.isEmpty()) {
            List<Employee> saved = employeeRepository.saveAll(employeeBatch);
            createSalariesForBatch(saved, random, salaryBatch);
            salaryRepository.saveAll(salaryBatch);
        }
    }

    private void createSalariesForBatch(List<Employee> employees, Random random, List<Salary> salaryBatch) {
        for (Employee employee : employees) {
            double[] range = SALARY_RANGES.get(employee.getCountry());
            double baseSalaryValue = range[0] + random.nextDouble() * (range[1] - range[0]);
            BigDecimal baseSalary = BigDecimal.valueOf(baseSalaryValue).setScale(2, RoundingMode.HALF_UP);
            BigDecimal bonus = BigDecimal.valueOf(baseSalaryValue * (random.nextDouble() * 0.2)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal deductions = BigDecimal.valueOf(baseSalaryValue * (random.nextDouble() * 0.05)).setScale(2, RoundingMode.HALF_UP);

            Salary salary = Salary.builder()
                    .employee(employee)
                    .baseSalary(baseSalary)
                    .bonus(bonus)
                    .deductions(deductions)
                    .currency(employee.getCurrency())
                    .effectiveDate(employee.getJoinDate())
                    .createdBy("SYSTEM")
                    .build();

            salaryBatch.add(salary);
        }
    }
}
