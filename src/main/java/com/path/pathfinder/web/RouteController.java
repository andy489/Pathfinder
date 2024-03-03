package com.path.pathfinder.web;

import com.path.pathfinder.model.dto.RouteAddDto;
import com.path.pathfinder.model.user.PathfinderUserDetails;
import com.path.pathfinder.service.RouteService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/routes")
public class RouteController extends GenericController {

    private static final String BINDING_RESULT_PATH = "org.springframework.validation.BindingResult.";

    private final RouteService routeService;

    @Autowired
    public RouteController(RouteService routeService) {

        this.routeService = routeService;
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

        routeService.addRoute(routeAddDto, userDetails.getId());

        return super.redirect("all");
    }

    @GetMapping("/all")
    public ModelAndView getAllRoutes(ModelAndView modelAndView) {

        modelAndView.addObject("allRoutesViewModel", routeService.getAllRoutes());

        return super.view("routes", modelAndView);
    }

    @GetMapping("/details/{id}")
    public ModelAndView getRouteDetails(@PathVariable(name = "id") Long routeId, ModelAndView modelAndView) {

        modelAndView.addObject("routeDetails", routeService.getRouteDetails(routeId));

        return super.view("route-details", modelAndView);
    }

    @Transactional
    @GetMapping("/{categoryName}")
    public ModelAndView getFilteredByCategoryRoutes(@PathVariable(name = "categoryName") String type,
                                                    ModelAndView modelAndView) {

        modelAndView.addObject("allRoutesViewModel", routeService.getAllRoutesWithCategory(type));

        return super.view("routes", modelAndView);
    }
}
