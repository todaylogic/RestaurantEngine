package com.langko.restaurantengine.staff;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.staff.dto.StaffRequest;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<StaffResponse> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable).map(StaffResponse::new);
    }

    public StaffResponse getStaffById(Long id) {
        return new StaffResponse(staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id)));
    }

    public StaffResponse createStaff(StaffRequest request) {
        Staff staff = Staff.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .phone(request.getPhone()).build();
        Staff saved = staffRepository.save(staff);
        log.info("Created staff: {}", saved.getEmail());
        return new StaffResponse(saved);
    }

    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole());
        staff.setPhone(request.getPhone());
        return new StaffResponse(staffRepository.save(staff));
    }

    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        staffRepository.delete(staff);
        log.info("Deleted staff: {}", id);
    }
}
