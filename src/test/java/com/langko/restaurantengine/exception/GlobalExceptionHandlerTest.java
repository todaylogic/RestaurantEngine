package com.langko.restaurantengine.exception;

import com.langko.restaurantengine.common.ApiResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new ResourceNotFoundException("Staff not found"));
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isEqualTo("Staff not found");
    }

    @Test
    void handleConflict_returns409() {
        var response = handler.handleConflict(
            new org.springframework.dao.DataIntegrityViolationException("dup"));
        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void handleGeneral_returns500() {
        var response = handler.handleGeneral(new RuntimeException("boom"));
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
