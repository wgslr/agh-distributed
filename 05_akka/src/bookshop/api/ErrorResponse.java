package bookshop.api;

public class ErrorResponse {

    public enum ErrorType {
        NOT_FOUND;
    }

    public final ErrorType errorType;

    public ErrorResponse(ErrorType errorType) {
        this.errorType = errorType;
    }
}
