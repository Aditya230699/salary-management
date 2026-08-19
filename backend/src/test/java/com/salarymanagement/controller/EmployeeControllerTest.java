package com.salarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salarymanagement.dto.CreateEmployeeRequest;
import com.salarymanagement.dto.EmployeeDTO;
import com.salarymanagement.security.JwtTokenProvider;
import com.salarymanagement.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    private EmployeeDTO createTestDTO() {
        return EmployeeDTO.builder()
                .id(1L)
                .employeeId("EMP-00001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@acme.com")
                .designation("Software Engineer")
                .departmentName("Engineering")
                .country("USA")
                .currency("USD")
                .joinDate(LocalDate.of(2022, 1, 15))
                .status("ACTIVE")
                .currentSalary(new BigDecimal("100000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /api/employees - should return paginated list")
    @WithMockUser(roles = "HR_MANAGER")
    void getEmployees_ReturnsPagedResult() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(List.of(createTestDTO()));
        when(employeeService.getEmployees(any(), any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].email").value("john.doe@acme.com"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - should return employee details")
    @WithMockUser(roles = "HR_MANAGER")
    void getEmployee_ExistingId_ReturnsEmployee() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(createTestDTO());

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value("EMP-00001"))
                .andExpect(jsonPath("$.departmentName").value("Engineering"));
    }

    @Test
    @DisplayName("POST /api/employees - should create employee with valid data")
    @WithMockUser(roles = "HR_MANAGER")
    void createEmployee_ValidRequest_ReturnsCreated() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@acme.com")
                .designation("Senior Engineer")
                .departmentId(1L)
                .country("USA")
                .currency("USD")
                .joinDate(LocalDate.of(2024, 3, 1))
                .baseSalary(new BigDecimal("120000"))
                .build();

        EmployeeDTO responseDTO = EmployeeDTO.builder()
                .id(2L)
                .employeeId("EMP-00002")
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@acme.com")
                .designation("Senior Engineer")
                .departmentName("Engineering")
                .country("USA")
                .currency("USD")
                .joinDate(LocalDate.of(2024, 3, 1))
                .status("ACTIVE")
                .currentSalary(new BigDecimal("120000"))
                .createdAt(LocalDateTime.now())
                .build();

        when(employeeService.createEmployee(any(), any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.currentSalary").value(120000));
    }

    @Test
    @DisplayName("POST /api/employees - should reject invalid data")
    @WithMockUser(roles = "HR_MANAGER")
    void createEmployee_InvalidRequest_ReturnsBadRequest() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .firstName("")  // blank - invalid
                .email("not-an-email")  // invalid format
                .build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/employees - should return 401 without auth")
    void getEmployees_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
