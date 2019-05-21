package bookshop.api;

import java.io.Serializable;

public class ErrorResponse implements Serializable {

    public enum ErrorType {
        NOT_FOUND,
        DB_UNAVAILABLE,
        INTERNAL_SERVER_ERROR,
    }

    public final ErrorType errorType;

    public ErrorResponse(ErrorType errorType) {
        this.errorType = errorType;
    }
}
