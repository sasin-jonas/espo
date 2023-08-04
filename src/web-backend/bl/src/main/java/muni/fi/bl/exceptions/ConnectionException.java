package muni.fi.bl.exceptions;

/**
 * Represents a problem with a connection to an external server (like ElasticSearch, dataloader)
 * Is translated to status 503 (service unavailable)
 */
public class ConnectionException extends RuntimeException {

    public static final String ELASTIC_CONNECTION_ERROR = "Elasticsearch connection error";

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
