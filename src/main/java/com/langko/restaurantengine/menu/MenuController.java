package com.langko.restaurantengine.menu;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.menu.dto.MenuItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<MenuCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(menuService.getAllCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuCategory>> createCategory(
            @Valid @RequestBody MenuCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.createCategory(request)));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<Page<MenuItem>>> getItems(
            @RequestParam(required = false) Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getAllItems(categoryId, pageable)));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getItemById(id)));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuItem>> createItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.createItem(request)));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuItem>> updateItem(
            @PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateItem(id, request)));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
