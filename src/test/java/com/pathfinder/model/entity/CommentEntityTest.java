package com.pathfinder.model.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentEntityTest {

    @Test
    void defaultApprovedIsFalse() {
        assertThat(new CommentEntity().getApproved()).isFalse();
    }

    @Test
    void toggleApprove_falseBecomesTrue() {
        CommentEntity c = new CommentEntity();
        c.toggleApprove();
        assertThat(c.getApproved()).isTrue();
    }

    @Test
    void toggleApprove_trueBecomesFalse() {
        CommentEntity c = new CommentEntity();
        c.setApproved(true);
        c.toggleApprove();
        assertThat(c.getApproved()).isFalse();
    }

    @Test
    void toggleApprove_nullTreatedAsFalse_becomesTrue() {
        CommentEntity c = new CommentEntity();
        c.setApproved(null);
        c.toggleApprove();
        assertThat(c.getApproved()).isTrue();
    }
}
