package com.pathfinder.web;

import com.pathfinder.service.PictureService;
import com.pathfinder.service.RouteService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController extends GenericController {

    private final RouteService routeService;

    private final PictureService pictureService;


    @Autowired
    public HomeController(RouteService routeService, PictureService pictureService) {

        this.routeService = routeService;
        this.pictureService = pictureService;
    }

    @GetMapping({"/", "/index", "/home"})
    @Transactional
    public ModelAndView home(ModelAndView modelAndView) {

        // modelAndView.addObject("mostCommentedRoute", routeService.getMostCommented());
        modelAndView.addObject("mostCommentedRoute", routeService.getRandFromMostCommented());
        modelAndView.addObject("sampleRoutePictures", pictureService.getSampleRoutePicturesUrls());

        return super.view("index", modelAndView);
    }

    @GetMapping("/about")
    public ModelAndView getAbout() {

        return super.view("about");
    }


}
