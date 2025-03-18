package org.cyberrealm.tech.muvio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MediaSyncException.class)
    public ResponseEntity<Object> handleMovieSyncException(MediaSyncException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TmdbServiceException.class)
    public ResponseEntity<Object> handleTmdbServiceException(TmdbServiceException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(exception.getMessage());
    }

    @ExceptionHandler(CategoryProcessingException.class)
    public ResponseEntity<Object> handleCategoryProcessingException(
            CategoryProcessingException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MovieProcessingException.class)
    public ResponseEntity<Object> handleMovieProcessingException(
            MovieProcessingException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NetworkRequestException.class)
    public ResponseEntity<Object> handleMovieProcessingException(
            NetworkRequestException exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + exception.getMessage());
    }
}
