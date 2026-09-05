package com.pathfinder.web;

import org.springframework.web.servlet.ModelAndView;

public abstract class GenericController {

    private static final String REDIRECT = "redirect:";

    protected static final String BINDING_RESULT_PATH = "org.springframework.validation.BindingResult.";

    protected ModelAndView view(String viewName, ModelAndView modelAndView) {
        modelAndView.setViewName(viewName);
        return modelAndView;
    }

    protected ModelAndView view(String viewName) {
        return this.view(viewName, new ModelAndView());
    }

    protected ModelAndView redirect(String redirectUrl) {
        return this.view(REDIRECT + redirectUrl);
    }
}
