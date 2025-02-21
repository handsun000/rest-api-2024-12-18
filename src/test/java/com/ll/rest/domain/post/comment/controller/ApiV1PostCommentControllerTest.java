package com.ll.rest.domain.post.comment.controller;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.domain.post.comment.entity.PostComment;
import com.ll.rest.domain.post.post.controller.ApiV1PostController;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ApiV1PostCommentControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private PostService postService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("다건 조회")
    void t1() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/v1/posts/1/comments")
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk());

        List<PostComment> comments = postService.findById(1).get().getComments();

        for (int i = 0; i<comments.size(); i++) {
            PostComment comment = comments.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(comment.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(comment.getCreateDate().toString().substring(0, 10))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(comment.getModifyDate().toString().substring(0, 10))))
                    .andExpect(jsonPath("$[%d].authorId".formatted(i)).value(comment.getAuthor().getId()))
                    .andExpect(jsonPath("$[%d].authorName".formatted(i)).value(comment.getAuthor().getName()))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(comment.getContent()));
        }
    }

    @Test
    @DisplayName("댓글 삭제")
    void t2() throws Exception {

        Member member = memberService.findByUsername("user2").get();
        String accessToken = memberService.genAccessToken(member);

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/v1/posts/1/comments/1")
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 삭제되었습니다."));
    }

    @Test
    @DisplayName("댓글 수정")
    void t3() throws Exception {

        Member member = memberService.findByUsername("user2").get();
        String accessToken = memberService.genAccessToken(member);

        ResultActions resultActions = mvc
                .perform(
                        put("/api/v1/posts/1/comments/1")
                                .header("Authorization", "Bearer " + accessToken)
                                .content("""
                                            {
                                                "content" : "내용 new"
                                            }
                                        """)
                                .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                )
                .andDo(print())
                .andExpect(handler().handlerType(ApiV1PostCommentController.class))
                .andExpect(handler().methodName("items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1번 댓글이 수정되었습니다."));
    }
}
