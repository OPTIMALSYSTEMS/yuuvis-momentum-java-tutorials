
package com.os.services.interceptor.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class DefaultServicesResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(value = {Throwable.class})
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request)
    {
               
        return super.handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
    }

}
