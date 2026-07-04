package co.edu.escuelaing.alphaeci.matching_service.entrypoints.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.InvalidInputException;
import co.edu.escuelaing.alphaeci.matching_service.domain.exceptions.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidationErrors_returnsBadRequest() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "name", "Name is required");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("name");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void handleValidationErrors_noFieldErrors_returnsDefaultMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleBusinessErrors_notFound_returns404() {
        NotFoundException ex = new NotFoundException("Category not found");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Category not found");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleBusinessErrors_invalidInput_returns400() {
        InvalidInputException ex = new InvalidInputException("Already exists");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    void handleGeneralErrors_returns500() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    void errorResponse_hasTimestamp() {
        NotFoundException ex = new NotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessErrors(ex);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
