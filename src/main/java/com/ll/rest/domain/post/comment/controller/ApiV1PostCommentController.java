package com.ll.rest.domain.post.comment.controller;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.post.comment.dto.PostCommentDto;
import com.ll.rest.domain.post.comment.entity.PostComment;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rq.Rq;
import com.ll.rest.global.rsData.RsData;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class ApiV1PostCommentController {
//    @Autowired
//    @Lazy
//    private ApiV1PostCommentController self;

    private final PostService postService;
    private final Rq rq;

    @GetMapping
    public List<PostCommentDto> getItems(
            @PathVariable long postId
    ) {
        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        return post
                .getCommentsByOrderByIdDesc()
                .stream()
                .map(PostCommentDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PostCommentDto getItem(
            @PathVariable long id,
            @PathVariable long postId
    ) {
        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        return post
                .getCommentById(id)
                .map(PostCommentDto::new)
                .orElseThrow(() -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(id)));

    }

    record PostCommentWriteReqBody(
            @NotBlank
            @Length(min = 4)
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public RsData<PostCommentDto> writeItem(
            @PathVariable long postId,
            @RequestBody @Valid PostCommentWriteReqBody reqBody
    ) {
        Member author = rq.checkAuthentication();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment comment = post.addComment(author, reqBody.content);

        postService.flush();

        return new RsData<>(
                "201-1",
                "%d번 댓글이 작성되었습니다.".formatted(postId),
                new PostCommentDto(comment)
        );
    }

    record PostCommentModifyReqBody(
            @NotBlank
            @Length(min = 4)
            String content
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostCommentDto> modifyItem(
            @PathVariable long postId,
            @PathVariable long id,
            @RequestBody @Valid PostCommentModifyReqBody reqBody
    ) {
        Member author = rq.checkAuthentication();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("404-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment comment = post.getCommentById(id)
                .orElseThrow(() -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(id)));

        if (!comment.getAuthor().equals(author)) {
            throw new ServiceException("403-1", "작성자만 수정할 수 있습니다.");
        }

        comment.modify(reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 수정되었습니다.".formatted(postId),
                new PostCommentDto(comment)
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteItem(
            @PathVariable long id,
            @PathVariable long postId
    ) {
        Member member = rq.checkAuthentication();

        Post post = postService.findById(postId)
                .orElseThrow(() -> new ServiceException("401-1", "%d번 글은 존재하지 않습니다.".formatted(postId)));

        PostComment comment = post.getCommentById(id)
                .orElseThrow(() -> new ServiceException("404-2", "%d번 댓글은 존재하지 않습니다.".formatted(id)));

        post.removeComment(comment);

        return new RsData<>(
                "200-1",
                "%d번 댓글이 삭제되었습니다.".formatted(id)
        );
    }
}
