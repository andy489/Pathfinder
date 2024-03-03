package com.pathfinder.web;

import com.pathfinder.model.enumerated.UserRoleEnum;
import com.pathfinder.model.user.PathfinderUserDetails;
import com.pathfinder.service.SuperuserService;
import com.pathfinder.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/superuser")
public class SuperUserController extends GenericController {

    private final SuperuserService superuserService;

    private final CommentService commentService;

    public SuperUserController(SuperuserService superuserService, CommentService commentService) {

        this.superuserService = superuserService;
        this.commentService = commentService;
    }

    // COMMENTS
    @GetMapping("/comments")
    public ModelAndView getComments(ModelAndView modelAndView) {

        modelAndView.addObject("superuserCommentsData", superuserService.getRouteComments());

        return super.view("superuser-comments", modelAndView);
    }

    @DeleteMapping("/comments/{id}")
    public @ResponseBody ResponseEntity<Void> deleteComment(@PathVariable("id") Long commentId) {

        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/comments/{id}")
    public @ResponseBody ResponseEntity<Void> approveRejectComment(@PathVariable("id") Long commentId) {

        commentService.toggleApproveComment(commentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/comments/route/{id}")
    public @ResponseBody ResponseEntity<List<Long>> deleteAllRouteComments(@PathVariable("id") Long routeId,
                                                                           @RequestParam("approved") Boolean approved) {

        List<Long> commentIds = superuserService.deleteAllRouteComments(routeId, approved);

        return new ResponseEntity<>(commentIds, HttpStatus.OK);
    }

    @PatchMapping("/comments/route/{id}")
    public @ResponseBody ResponseEntity<List<Long>> approveRejAllRouteComments(@PathVariable("id") Long routeId,
                                                                               @RequestParam("approved") Boolean approved) {

        List<Long> commentIds = superuserService.approveRejAllRouteComments(routeId, approved);

        return new ResponseEntity<>(commentIds, HttpStatus.OK);
    }

    // PERMISSIONS
    @GetMapping("/permissions")
    public ModelAndView getUsers(ModelAndView modelAndView) {

        modelAndView.addObject("superuserUsersData", superuserService.getUserRoles());

        return super.view("superuser-permissions", modelAndView);
    }

    @PatchMapping("/permissions/{id}")
    public @ResponseBody ResponseEntity<Void>
    changePermModeratorUser(@PathVariable("id") Long userId,
                            @RequestParam("from") String from,
                            @RequestParam("to") String to,
                            @CurrentSecurityContext(expression = "authentication.principal")
                            PathfinderUserDetails principal) {

        if (userId.equals(principal.getId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        from = from.toUpperCase();
        to = to.toUpperCase();

        superuserService.togglePermUser(userId, UserRoleEnum.valueOf(from), UserRoleEnum.valueOf(to));

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
