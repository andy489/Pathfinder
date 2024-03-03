package com.pathfinder.model.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString
public class CommentView {

    private Long commentId;

    private LocalDateTime created;

    private LocalDateTime modified;

    private String comment;

    private String authorName;

    private Boolean approved;

}
