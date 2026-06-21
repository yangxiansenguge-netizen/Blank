package com.blank.app.util;

import com.blank.app.dto.response.ApiResponse;
import com.blank.app.dto.response.PageResponse;

import java.util.List;

public class ResponseUtils {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.error(message);
    }

    public static <T> ApiResponse<PageResponse<T>> paginate(List<T> list, long total, int page, int pageSize) {
        return PageResponse.paginate(list, total, page, pageSize);
    }
}
