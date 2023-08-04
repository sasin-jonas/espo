package muni.fi.bl.exceptions;

/**
 * Is raised when an entity to be deleted/updated/retrieved is not found
 * Translates to error status 404 (not found)
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

}
