package egovframework.exception;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String messageCode;

    // 1. 성공: 데이터(T)를 직접 넘기는 경우 (가장 많이 사용)
    public static <T> ApiResponse<T> success(T data) {
        return success(data, ResponseCode.SUCCESS.getCode());
    }

    // 2. 성공: 데이터와 코드 모두 지정
    public static <T> ApiResponse<T> success(T data, String messageCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        response.setMessageCode(messageCode);
        return response;
    }

    // 3. 성공: 데이터 없이 성공 여부와 코드만 보낼 때 (ApiResponse<Void>로 활용)
    public static ApiResponse<Void> success() {
        return success(null, ResponseCode.SUCCESS.getCode());
    }

    // 4. 실패: 메시지와 코드 전달
    public static ApiResponse<Void> fail(String message, String messageCode) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setMessageCode(messageCode);
        return response;
    }
}