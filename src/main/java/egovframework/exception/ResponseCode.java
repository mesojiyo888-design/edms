package egovframework.exception;

public enum ResponseCode {
    // 성공 코드
    SUCCESS("200", "성공"),

    // 실패 코드
    FAIL_DB("ERR_500_DB", "데이터베이스 오류"),
    FAIL_NOT_FOUND("ERR_404", "페이지를 찾을 수 없습니다"),
    FAIL_METHOD_NOT_ALLOWED("ERR_405", "허용되지 않은 요청 메서드입니다"),
    FAIL_UNAUTHORIZED("ERR_401", "인증 필요"),
    FAIL_FORBIDDEN("ERR_403", "권한 없음"),
    FAIL_SYSTEM("ERR_500", "시스템 오류");

    private final String code;
    private final String message;

    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}