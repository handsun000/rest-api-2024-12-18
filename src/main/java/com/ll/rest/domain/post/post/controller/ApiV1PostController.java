package com.ll.rest.domain.post.post.controller;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.domain.post.post.dto.PostDto;
import com.ll.rest.domain.post.post.dto.PostWithContentDto;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rq.Rq;
import com.ll.rest.global.rsData.RsData;
import com.ll.rest.standard.page.dto.PageDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jdk.jfr.Frequency;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final Rq rq;

    @GetMapping("/mine")
    public PageDto<PostDto> mine(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Member member = rq.checkAuthentication();

        return new PageDto<>(
                postService.findByAuthorPaged(member, searchKeywordType, searchKeyword, page, pageSize)
                        .map(PostDto::new)
        );
    }

    @GetMapping
    public PageDto<PostDto> items(
            @RequestParam(defaultValue = "title") String searchKeywordType,
            @RequestParam(defaultValue = "") String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return new PageDto<>(
                postService.findByListedPaged(true, searchKeywordType, searchKeyword, page, pageSize)
                        .map(PostDto::new)
        );
    }

    @GetMapping("{id}")
    public PostWithContentDto item(
            @PathVariable long id
    ) {
        Post post = postService.findById(id).get();

        if (!post.isPublished()) {
            Member member = rq.checkAuthentication();

            post.checkActorCanRead(member);
        }

        return new PostWithContentDto(post);
    }

    record PostWriteReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,
            boolean published,
            boolean listed
    ) {
    }

    @PostMapping
    public RsData<PostWithContentDto> write(
            @RequestBody @Valid PostWriteReqBody reqBody,
            @AuthenticationPrincipal UserDetails user
            ) {

        Member member = null;

        if (user != null) {
            member  = rq.getActorByUsername(user.getUsername());
        }

        Post post = postService.write(member, reqBody.title, reqBody.content, reqBody.published, reqBody.listed);

        return new RsData<>(
                "201-1",
                "%s번 글이 작성 되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
        );
    }

    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2, max = 100)
            String title,
            @NotBlank
            @Length(min = 2, max = 10000000)
            String content,
            boolean published,
            boolean listed
    ) {
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostWithContentDto> modify(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member member = rq.checkAuthentication();
        Post post = postService.findById(id).get();

        post.checkActorCanModify(member);

        post.modify(reqBody.title, reqBody.content, reqBody.published, reqBody.listed);

        postService.flush();

        return new RsData<>(
                "200-1",
                "%s번 글이 수정되었습니다.".formatted(post.getId()),
                new PostWithContentDto(post)
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
