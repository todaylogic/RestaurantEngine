package com.langko.restaurantengine.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String error;
    private final Integer status;

    private ApiResponse(boolean success, T data, String message, String error, Integer status) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.status = status;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getError() { return error; }
    public Integer getStatus() { return status; }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK", null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null);
    }

    public static ApiResponse<Void> error(String error, int status) {
        return new ApiResponse<>(false, null, null, error, status);
    }
}
