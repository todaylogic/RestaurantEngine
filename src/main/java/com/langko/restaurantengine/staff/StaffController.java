package com.langko.restaurantengine.staff;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.staff.dto.StaffRequest;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> getAllStaff(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getAllStaff(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getStaffById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffService.createStaff(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffService.updateStaff(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
