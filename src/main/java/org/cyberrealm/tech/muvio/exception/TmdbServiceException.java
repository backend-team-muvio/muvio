package org.cyberrealm.tech.muvio.exception;

public class TmdbServiceException extends RuntimeException {
    public TmdbServiceException(String message) {
        super(message);
    }

    public TmdbServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
