package egovframework.exception;

public class EdmsException extends RuntimeException {
    private final String messageCode;
    private final int status;

    public EdmsException(String message, String messageCode, int status) {
        super(message);
        this.messageCode = messageCode;
        this.status = status;
    }

    public String getMessageCode() { return messageCode; }
    public int getStatus() { return status; }
}