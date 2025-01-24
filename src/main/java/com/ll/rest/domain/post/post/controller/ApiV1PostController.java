package com.ll.rest.domain.post.post.controller;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.domain.post.post.dto.PostDto;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rq.Rq;
import com.ll.rest.global.rsData.RsData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Frequency;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;

    @GetMapping("{id}")
    public PostDto item(
            @PathVariable long id
    ) {

        return new PostDto(postService.findById(id).get());
    }

    record PostWriteReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,
            boolean published
    ) {
    }

    @PostMapping
    public RsData<PostDto> write(
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {
        Member member = rq.checkAuthentication();
        Post post = postService.write(member, reqBody.title, reqBody.content, reqBody.published);

        return new RsData<>(
                "201-1",
                "%s번 글이 작성 되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content
    ){}

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostDto> modify(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member member = rq.checkAuthentication();
        Post post = postService.findById(id).get();

        post.checkActorCanModify(member);

        post.modify(reqBody.title, reqBody.content);

        postService.flush();

        return new RsData<>(
                "200-1",
                "%s번 글이 수정되었습니다.".formatted(post.getId()),
                new PostDto(post)
        );
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @PathVariable long id
    ) {
        Member member = rq.checkAuthentication();
        Post post = postService.findById(id).get();

        post.checkActorCanDelete(member);

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%s번 글이 삭제되었습니다.".formatted(id)
        );
    }
}
