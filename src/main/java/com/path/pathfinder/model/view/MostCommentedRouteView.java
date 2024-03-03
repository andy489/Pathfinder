package com.path.pathfinder.model.view;

import com.path.pathfinder.model.entity.CommentEntity;
import com.path.pathfinder.model.entity.PictureEntity;
import com.path.pathfinder.model.entity.RouteEntity;
import com.path.pathfinder.service.RouteService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString
public class MostCommentedRouteView {

    private Long id;

    private String name;

    private String imageUrl;

    private Long approvedComments;

    public static MostCommentedRouteView of(RouteEntity routeEntity) {
        return MostCommentedRouteView.of(
                routeEntity.getId(),
                routeEntity.getName(),
                routeEntity.getPictures().stream().map(PictureEntity::getUrl).findAny().orElseGet(
                        () -> RouteService.DEFAULT_PIC_URL),
                routeEntity.getComments().stream().filter(CommentEntity::getApproved).count()
        );
    }
}
