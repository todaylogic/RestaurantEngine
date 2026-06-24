# Final Fix Report

**Date:** 2026-06-24  
**Commit message:** fix: address final review findings - TOCTOU, table guard, transaction annotations

## Fixes Applied

### Fix 1 (Critical) — First-admin TOCTOU in AuthService
- Moved the `isFirstStaff()` check and the role guard into `AuthService.register()`, now annotated `@Transactional`, eliminating the race window between count check and insert.
- `AuthService.register(request, currentUser)` now returns `StaffResponse` directly.
- Removed the `isFirstStaff()` method entirely.
- `AuthController.register` now delegates entirely to the service (no logic in controller).

### Fix 2 (Critical) — deleteTable active-order guard
- Added `boolean existsByTableId(Long tableId)` to `OrderRepository`.
- Injected `OrderRepository` into `TableService` via Lombok `@RequiredArgsConstructor`.
- Added guard at the start of `deleteTable`: throws `IllegalStateException` if orders reference the table.
- Added `@ExceptionHandler(IllegalStateException.class)` to `GlobalExceptionHandler` returning HTTP 409 CONFLICT.

### Fix 3 (Important) — createOrder table availability guard
- In `OrderService.createOrder`, added check: if `table.getStatus() != TableStatus.AVAILABLE`, throws `IllegalStateException("Table X is not available")`.

### Fix 4 (Important) — updateStaff forces password re-encode
- Removed `@NotBlank` from `password` in `StaffRequest` (field is now nullable/optional).
- In `StaffService.updateStaff`, password is only re-encoded when provided and non-blank.
- `RegisterRequest` retains `@NotBlank` on password (bootstrap registration always requires one).

### Fix 5 (Important) — Missing @Transactional on service write methods
- `MenuService`: added `@Transactional(readOnly=true)` to `getAllCategories`, `getAllItems`, `getItemById`; `@Transactional` to `createCategory`, `createItem`, `updateItem`, `deleteItem`.
- `TableService`: added `@Transactional(readOnly=true)` to `getAllTables`, `getTableById`; `@Transactional` to `createTable`, `updateTable`, `deleteTable`.
- `StaffService`: added `@Transactional(readOnly=true)` to `getAllStaff`, `getStaffById`; `@Transactional` to `createStaff`, `updateStaff`, `deleteStaff`.

### Fix 6 (Important) — updateStatus uses lazy proxy for table
- In `OrderService.updateStatus`, replaced `order.getTable().setStatus(...)` with an explicit `tableRepository.findById(order.getTable().getId())` call before modifying and saving the table entity.

## Test Results

**22/22 tests pass** — BUILD SUCCESS

No test modifications were required. `AuthControllerTest.register_firstStaff_returnsOkAndNoPassword` continues to work because Spring MVC injects `null` for `@AuthenticationPrincipal` on unauthenticated requests, which correctly triggers the first-staff bootstrap path.
