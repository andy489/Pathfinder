package com.path.pathfinder.model.entity;

import com.path.pathfinder.model.enumerated.LevelEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "routes", uniqueConstraints = {
        @UniqueConstraint(name = "UniqueRouteNameAndAuthorUser", columnNames = {"name", "author_id"})
})
@Getter
@Setter
@Accessors(chain = true)
public class RouteEntity extends GenericEntity {

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String gpxCoordinates;

    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    private String videoUrl;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private UserEntity author;

    @OneToMany(targetEntity = CommentEntity.class, mappedBy = "route", orphanRemoval = true, /*fetch = FetchType.EAGER,*/
            cascade = {CascadeType.ALL})
    private Set<CommentEntity> comments = new HashSet<>();


    @OneToMany(targetEntity = PictureEntity.class, mappedBy = "route", fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    private Set<PictureEntity> pictures;


    @ManyToMany
    @JoinTable(name = "routes_categories",
            joinColumns =
            @JoinColumn(name = "route_id", referencedColumnName = "id"),
            inverseJoinColumns =
            @JoinColumn(name = "category_id", referencedColumnName = "id")
    )
    private Set<CategoryEntity> categories = new HashSet<>();
}
