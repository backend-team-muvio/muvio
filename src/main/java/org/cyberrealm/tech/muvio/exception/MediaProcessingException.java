package org.cyberrealm.tech.muvio.exception;

public class MediaProcessingException extends RuntimeException {
    public MediaProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MediaProcessingException(String message) {
        super(message);
    }
}
