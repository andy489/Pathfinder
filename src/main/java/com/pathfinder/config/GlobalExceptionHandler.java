package com.pathfinder.config;

import com.pathfinder.exception.GpxProcessingException;
import com.pathfinder.exception.RouteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartExceed(MultipartException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("multipartExceed", ex.getMessage());
        return "redirect:/routes/add";
    }

    @ExceptionHandler({RouteNotFoundException.class, NoSuchElementException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "error";
    }

    @ExceptionHandler(GpxProcessingException.class)
    public ResponseEntity<String> handleGpxProcessing(GpxProcessingException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body("Invalid parameter '" + ex.getName() + "': " + ex.getValue());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getReason());
        if (ex.getStatusCode() == HttpStatus.FORBIDDEN) {
            return "redirect:/";
        }
        return "error";
    }
}

