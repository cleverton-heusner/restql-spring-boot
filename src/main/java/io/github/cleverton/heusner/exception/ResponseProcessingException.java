package io.github.cleverton.heusner.exception;

public class ResponseProcessingException extends RuntimeException {

    public ResponseProcessingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
