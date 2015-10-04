package de.helfenkannjeder.come2help.server.rest.exceptionhandling;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;
import de.helfenkannjeder.come2help.server.service.exception.DuplicateResourceException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestExceptionResolverTest {

    private final RestExceptionResolver restExceptionResolver = new RestExceptionResolver();

    @Test
    public void exceptionShouldBeAnsweredWithInternalServerError() {
        ResponseEntity<ErrorResponse> response = getResponseEntityForException();

        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    public void exceptionResponseBodyShouldContainIncidentId() {
        ErrorResponse response = getResponseEntityForException().getBody();

        assertThat(response.incidentId, notNullValue());
    }

    @Test
    public void exceptionResponseBodyShouldContainDefaultHttpStatusDescription() {
        ErrorResponse response = getResponseEntityForException().getBody();

        assertThat(response.description, is(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));
    }

    @Test
    public void duplicateResourceExceptionShouldBeAnsweredWithConflict() {
        ResponseEntity<ErrorResponse> response = getResponseEntityForDuplicateResourceException("xy already exists");

        assertThat(response.getStatusCode(), is(HttpStatus.CONFLICT));
    }

    @Test
    public void duplicateResourceExceptionResponseBodyShouldContainExceptionMessageAsDescription() {

        ErrorResponse response = getResponseEntityForDuplicateResourceException("xy already exists").getBody();

        assertThat(response.description, is("xy already exists"));
    }

    @Test
    public void methodArgumentNotValidExceptionShouldBeAnsweredWithBadRequest() {
        ResponseEntity<ErrorResponse> response = getResponseEntityForMethodArgumentNotValidException();

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void methodArgumentNotValidExceptionResponseBodyShouldContainClientError() {
        ErrorResponse response = getResponseEntityForMethodArgumentNotValidException().getBody();

        assertThat(response.clientErrors, notNullValue());
        assertThat(response.clientErrors.size(), is(1));
        assertThat(response.clientErrors.get(0).path, is("fieldName"));
        assertThat(response.clientErrors.get(0).value, is("rejectedValue"));
        assertThat(response.clientErrors.get(0).code, is("invalid.value"));
    }

    private ResponseEntity<ErrorResponse> getResponseEntityForMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Lists.newArrayList(new FieldError("objName", "fieldName", "rejectedValue", false, null, null, "invalid.value")));

        return restExceptionResolver.resolveMethodArgumentNotValidException(exception);
    }

    private ResponseEntity<ErrorResponse> getResponseEntityForDuplicateResourceException(String message) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        return restExceptionResolver.resolveDuplicateResourceException(request, new DuplicateResourceException(message));
    }

    private ResponseEntity<ErrorResponse> getResponseEntityForException() {
        return restExceptionResolver.resolveException(new Exception("an error"));
    }
}