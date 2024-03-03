package com.path.pathfinder.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MultipartException.class)
    public String handleMultipartExceed(MultipartException ex, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("multipartExceed", ex.getMessage());

        return "redirect:/routes/add";
    }
}
