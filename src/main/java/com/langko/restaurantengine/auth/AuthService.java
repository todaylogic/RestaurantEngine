package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.auth.dto.AuthResponse;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Staff staff = staffRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        String token = jwtUtil.generateToken(staff);
        log.info("Staff logged in: {}", staff.getEmail());
        return new AuthResponse(token, staff.getRole(), staff.getFirstName(), staff.getLastName());
    }

    public Staff register(RegisterRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        Staff staff = Staff.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .phone(request.getPhone())
            .build();
        Staff saved = staffRepository.save(staff);
        log.info("New staff registered: {}", saved.getEmail());
        return saved;
    }

    public boolean isFirstStaff() {
        return staffRepository.count() == 0;
    }
}
