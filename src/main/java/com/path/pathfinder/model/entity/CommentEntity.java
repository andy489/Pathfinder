package com.path.pathfinder.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Accessors(chain = true)
public class CommentEntity extends GenericEntity {

    @Column(name = "approved")
    private Boolean approved;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime created;

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime modified;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private UserEntity author;

    @ManyToOne
    private RouteEntity route;

    public void toggleApprove() {
        approved = !approved;
    }
}
