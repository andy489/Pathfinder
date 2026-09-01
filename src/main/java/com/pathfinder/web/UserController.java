package com.pathfinder.web;

import com.pathfinder.model.dto.UserEditDto;
import com.pathfinder.model.dto.UserRegistrationDto;
import com.pathfinder.service.UserService;
import com.pathfinder.model.user.PathfinderUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController extends GenericController {

    private final UserService userService;

    private final SecurityContextRepository securityContextRepository;

    public UserController(UserService userService, SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @ModelAttribute(name = "userRegistrationModel")
    public UserRegistrationDto initUserRegisterDto() {
        return new UserRegistrationDto();
    }

    @GetMapping("/login")
    public ModelAndView login() {

        return super.view("auth/login");
    }

    @GetMapping("/register")
    public ModelAndView register() {

        return super.view("auth/register");
    }

    @PostMapping("/register")
    public ModelAndView postRegister(
            @Valid @ModelAttribute(name = "userRegistrationModel") UserRegistrationDto userRegistrationDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userRegistrationModel", userRegistrationDto);
            redirectAttributes.addFlashAttribute(BINDING_RESULT_PATH + "userRegistrationModel", bindingResult);

            return super.redirect("/users/register");
        }

        // https://stackoverflow.com/questions/75618616/auto-outin-after-registration-spring-boot-3-spring-security-6
        userService.registerAndLogin(userRegistrationDto, successfulAuth -> {
            // populating security context
            SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();

            SecurityContext context = strategy.createEmptyContext();
            context.setAuthentication(successfulAuth);

            strategy.setContext(context);

            securityContextRepository.saveContext(context, request, response);
        });

        return super.redirect("/");
    }

    @PostMapping("/login-error")
    public ModelAndView onFailedLogin(
            RedirectAttributes redirectAttributes,
            @ModelAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY) String username) {

        redirectAttributes.addFlashAttribute("bad_credentials", true);
        redirectAttributes.addFlashAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY, username);

        return super.redirect("/users/login");
    }

    @ModelAttribute(name = "userEditModel")
    public UserEditDto initUserEditDto() {
        return new UserEditDto();
    }

    @GetMapping("/profile")
    public ModelAndView profile(@AuthenticationPrincipal PathfinderUserDetails userDetails,
                                ModelAndView modelAndView) {
        modelAndView.addObject("userEditModel",
                new UserEditDto().setFullName(userDetails.getFullName()).setEmail(userDetails.getEmail()));
        return super.view("profile", modelAndView);
    }

    @PostMapping("/profile/edit")
    public ModelAndView editProfile(
            @Valid @ModelAttribute(name = "userEditModel") UserEditDto userEditDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal PathfinderUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) {

        String newPw = userEditDto.getNewPassword();
        String confirmPw = userEditDto.getConfirmPassword();
        if (newPw != null && !newPw.isBlank() &&
                confirmPw != null && !newPw.equals(confirmPw)) {
            bindingResult.rejectValue("confirmPassword", "passwords.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userEditModel", userEditDto)
                    .addFlashAttribute(BINDING_RESULT_PATH + "userEditModel", bindingResult);
            return super.redirect("/users/profile");
        }

        try {
            Authentication newAuth = userService.updateProfile(userDetails.getId(), userEditDto);
            if (newAuth != null) {
                SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
                SecurityContext context = strategy.createEmptyContext();
                context.setAuthentication(newAuth);
                strategy.setContext(context);
                securityContextRepository.saveContext(context, request, response);
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("userEditModel", userEditDto)
                    .addFlashAttribute("editError", e.getMessage());
            return super.redirect("/users/profile");
        }
        redirectAttributes.addFlashAttribute("editSuccess", true);
        return super.redirect("/users/profile");
    }
}
