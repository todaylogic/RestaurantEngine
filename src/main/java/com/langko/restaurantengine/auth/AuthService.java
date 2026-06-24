package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.auth.dto.AuthResponse;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(StaffRepository staffRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Staff staff = staffRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        String token = jwtUtil.generateToken(staff);
        log.info("Staff logged in: {}", staff.getEmail());
        return new AuthResponse(token, staff.getRole(), staff.getFirstName(), staff.getLastName());
    }

    @Transactional
    public StaffResponse register(RegisterRequest request, Staff currentUser) {
        boolean isFirst = staffRepository.count() == 0;
        if (!isFirst) {
            if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Only ADMIN can register new staff");
            }
        }
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        Staff staff = new Staff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole());
        staff.setPhone(request.getPhone());
        Staff saved = staffRepository.save(staff);
        log.info("New staff registered: {}", saved.getEmail());
        return new StaffResponse(saved);
    }
}
