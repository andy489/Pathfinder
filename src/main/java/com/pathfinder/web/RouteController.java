package com.pathfinder.web;

import com.pathfinder.model.dto.RouteAddDto;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.model.view.RouteDetailsView;
import com.pathfinder.service.RouteService;
import com.pathfinder.service.TranslationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Controller
@RequestMapping("/routes")
public class RouteController extends GenericController {

    private final RouteService routeService;
    private final TranslationService translationService;

    public RouteController(RouteService routeService, TranslationService translationService) {
        this.routeService = routeService;
        this.translationService = translationService;
    }

    @ModelAttribute(name = "routeAddModel")
    public RouteAddDto initRouteAddDto() {
        return new RouteAddDto();
    }

    @GetMapping("/add")
    public ModelAndView getRouteAdd() {
        return super.view("add-route");
    }

    @PostMapping("/add")
    public ModelAndView addNewRoute(
            @Valid @ModelAttribute(name = "routeAddModel") RouteAddDto routeAddDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal PathfinderUserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("routeAddModel", routeAddDto)
                    .addFlashAttribute(BINDING_RESULT_PATH + "routeAddModel", bindingResult);
            return super.redirect("/routes/add");
        }

        routeService.addRoute(routeAddDto, userDetails.getId(), userDetails.getUsername());

        return super.redirect("/routes/all");
    }

    @GetMapping("/all")
    public ModelAndView getAllRoutes(ModelAndView modelAndView,
                                    @RequestParam(name = "q", required = false) String query,
                                    @RequestParam(name = "page", defaultValue = "0") int page) {
        if (query != null && !query.isBlank()) {
            modelAndView.addObject("allRoutesViewModel", routeService.searchRoutes(query));
            modelAndView.addObject("routesPage", null);
        } else {
            Page<?> routesPage = routeService.getAllRoutesPaged(page);
            modelAndView.addObject("allRoutesViewModel", routesPage.getContent());
            modelAndView.addObject("routesPage", routesPage);
        }
        modelAndView.addObject("searchQuery", query != null ? query : "");
        return super.view("routes", modelAndView);
    }

    private static final Set<String> SUPPORTED_LANGS = Set.of("en", "bg");

    @GetMapping("/details/{id}")
    public ModelAndView getRouteDetails(@PathVariable(name = "id") Long routeId,
                                        @RequestParam(name = "lang", required = false) String langParam,
                                        ModelAndView modelAndView,
                                        HttpServletRequest request) {
        RouteDetailsView details = routeService.getRouteDetails(routeId);

        String lang = langParam;
        if (lang == null || lang.isBlank()) {
            Locale resolved = RequestContextUtils.getLocale(request);
            lang = resolved.getLanguage();
        }
        if (!SUPPORTED_LANGS.contains(lang)) lang = "en";

        if (!"bg".equals(lang)) {
            details.setName(translationService.translate(details.getName(), "bg", lang));
            details.setDescription(translationService.translate(details.getDescription(), "bg", lang));
        }
        modelAndView.addObject("routeDetails", details);
        return super.view("route-details", modelAndView);
    }

    @GetMapping("/{categoryName}")
    public ModelAndView getFilteredByCategoryRoutes(@PathVariable(name = "categoryName") String type,
                                                    ModelAndView modelAndView) {
        modelAndView.addObject("allRoutesViewModel", routeService.getAllRoutesWithCategory(type));
        return super.view("routes", modelAndView);
    }

    @DeleteMapping("/delete/{id}")
    public ModelAndView deleteRoute(@PathVariable Long id,
                                    @AuthenticationPrincipal PathfinderUserDetails principal,
                                    RedirectAttributes redirectAttributes) {
        boolean isAdmin = principal.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        routeService.deleteRoute(id, principal.getId(), isAdmin);
        redirectAttributes.addFlashAttribute("deleteSuccess", "Route deleted successfully.");
        return super.redirect("/routes/all");
    }
}
