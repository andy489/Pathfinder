package com.path.pathfinder.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "pictures")
@Getter
@Setter
@Accessors(chain = true)
public class PictureEntity extends GenericEntity {

    @Column(nullable = false)
    private String title;

    @Column(length = 1023, nullable = false, unique = true)
    private String url;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private UserEntity author;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private RouteEntity route;

}
