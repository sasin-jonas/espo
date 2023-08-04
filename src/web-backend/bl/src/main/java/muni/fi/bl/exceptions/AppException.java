package muni.fi.bl.exceptions;

/**
 * Represents useful information that is meant to be propagated to the user
 * Is translated to status 400 (bad request)
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
