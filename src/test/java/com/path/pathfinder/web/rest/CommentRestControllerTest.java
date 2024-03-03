package com.path.pathfinder.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.path.pathfinder.model.dto.CommentCreationDto;
import com.path.pathfinder.model.dto.CommentRestDto;
import com.path.pathfinder.model.view.CommentView;
import com.path.pathfinder.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentRestControllerTest {
    private static final Long ROUTE_ID = 1L;

    private static final ObjectMapper om = new ObjectMapper();

    private final MockMvc mockMvc;

    @Autowired
    public CommentRestControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @MockBean
    private CommentService commentService;

    @Test
    @WithMockUser
    void getCommentsCommentsExistAndReturnedAsJsonAndStatusIsOkTest() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        when(commentService.getAllCommentsForRoute(ROUTE_ID))
                .thenReturn(List.of(
                                CommentView.of(1L, now, now, "Test comment #1", "John Doe"),
                                CommentView.of(2L, now, now, "Test comment #2", "Foo Bar")
                        )
                );

        mockMvc.perform(get("/api/" + ROUTE_ID + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].authorName", is("John Doe")))
                .andExpect(jsonPath("$.[0].text", is("Test comment #1")))
                .andExpect(jsonPath("$.[1].authorName", is("Foo Bar")))
                .andExpect(jsonPath("$.[1].text", is("Test comment #2")));
    }

    @Test
    @WithUserDetails
    public void createCommentWithSampleDataAndCommentIsReturnedAsExpectedTest() throws Exception {
        when(commentService.createComment(any())).thenAnswer(interaction -> {
            CommentCreationDto commentCreationDto = interaction.getArgument(0);

            LocalDateTime now = LocalDateTime.now();

            return CommentView.of(
                    1L,
                    now,
                    now,
                    commentCreationDto.getComment(),
                    commentCreationDto.getUsername()
            );
        });

        CommentRestDto commentRestDto = new CommentRestDto()
                .setText("Test comment #1");

        mockMvc.perform(post("/api/" + ROUTE_ID + "/comments")
                        .content(om.writeValueAsString(commentRestDto))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.text", is("Test comment #1")));
    }

}
