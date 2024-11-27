package com.aishpam.esdminiproject.service;

import com.aishpam.esdminiproject.dto.EmployeeReqRes;
import com.aishpam.esdminiproject.entity.Employees;
import com.aishpam.esdminiproject.helper.JWTUtils;
import com.aishpam.esdminiproject.repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class EmployeeManagementService {

    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public EmployeeReqRes login(EmployeeReqRes loginRequest){
        EmployeeReqRes response = new EmployeeReqRes();
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                            loginRequest.getPassword()));
            // Fetch user details from repository
            Employees user = employeeRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT and refresh token
            String jwt = jwtUtils.generateToken(user);
            String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setTitle(user.getTitle());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }

    public EmployeeReqRes refreshToken(EmployeeReqRes refreshTokenRequest){
        EmployeeReqRes response = new EmployeeReqRes();
        try{

            String userEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());

            // Validate token and fetch user
            Employees employees = employeeRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), employees)) {
                var jwt = jwtUtils.generateToken(employees);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("24Hr");
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;

        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public EmployeeReqRes getAllEmployees() {
        EmployeeReqRes reqRes = new EmployeeReqRes();

        try {
            List<Employees> result = employeeRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setEmployeesList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successful");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No users found");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
            return reqRes;
        }
    }

    public EmployeeReqRes getEmployeeById(Integer id) {
        EmployeeReqRes reqRes = new EmployeeReqRes();
        try {
            Employees employee = employeeRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
            reqRes.setEmployees(employee);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Employees with id '" + id + "' found successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }

    @Transactional
    public EmployeeReqRes updateEmployee(Integer employeeId, Employees updatedEmployee) {
        EmployeeReqRes reqRes = new EmployeeReqRes();
        try {
            Employees existingUser = employeeRepo.findByEmployeeId(employeeId)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));

                existingUser.setEmployeeRefId(updatedEmployee.getEmployeeRefId());
                existingUser.setFirstName(updatedEmployee.getFirstName());
                existingUser.setLastName(updatedEmployee.getLastName());
                existingUser.setEmail(updatedEmployee.getEmail());
                existingUser.setTitle(updatedEmployee.getTitle());
                existingUser.setPhotographPath(updatedEmployee.getPhotographPath());
                existingUser.setDepartment(updatedEmployee.getDepartment());

                // Check if password is present in the request
                if (updatedEmployee.getPassword() != null && !updatedEmployee.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedEmployee.getPassword()));
                }

                Employees savedUser = employeeRepo.save(existingUser);
                reqRes.setEmployees(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return reqRes;
    }

    public EmployeeReqRes getMyInfo(String email){
        EmployeeReqRes reqRes = new EmployeeReqRes();
        try {
            Employees employees = employeeRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
                reqRes.setEmployees(employees);
                reqRes.setStatusCode(200);
                reqRes.setMessage("successful");
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return reqRes;
    }
}