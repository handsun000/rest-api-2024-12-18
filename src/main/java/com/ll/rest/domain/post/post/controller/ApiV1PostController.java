package com.ll.rest.domain.post.post.controller;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.domain.post.post.dto.PostDto;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rsData.RsData;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping
    public List<PostDto> getItems() {
        return postService.findAllByOrderByIdDesc()
                .stream()
                .map(PostDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PostDto getItem(
            @PathVariable long id
    ) {
        return postService.findById(id)
                .map(PostDto::new)
                .orElseThrow();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RsData<Void>> deleteItem(
            @PathVariable long id,
            @RequestHeader("actorId") long actorId,
            @RequestHeader("password") String password
    ) {
        Member actor = memberService.findById(actorId).get();

        if (!actor.getPassword().equals(password)) throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");

        Post post = postService.findById(id).get();

        if (!post.getAuthor().equals(actor)) throw new ServiceException("403-1", "작성자만 글을 삭제할 권한이 있습니다.");

        postService.delete(post);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new RsData<>(
                                "200-1",
                                "%d번 글이 삭제되었습니다.".formatted(id)
                        )
                );
    }

    record PostModifyReqBody(
            @NotBlank
            @Length(min = 2)
            String title,
            @NotBlank
            @Length(min = 2)
            String content,
            @NotNull
            Long authorId,
            @NotNull
            String password
    ) {

    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<PostDto> modifyItem(
            @PathVariable long id,
            @RequestBody @Valid PostModifyReqBody reqBody
    ) {
        Member author = memberService.findById(reqBody.authorId).get();

        if (!author.getPassword().equals(reqBody.password)) throw new ServiceException("401-1", "비밀번호가 일치하지 않습니다.");

        Post post = postService.findById(id).get();

        if (!post.getAuthor().equals(author)) throw new ServiceException("403-1", "작성자만 글을 수정할 권한이 있습니다.");

        postService.modify(post, reqBody.title, reqBody.content);

        return new RsData<>(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(id),
                new PostDto(post)
        );
    }

    record PostWriteReqBody(
            @NotBlank
            @Length(min = 2)
            String title,
            @NotBlank
            @Length(min = 2)
            String content,
            @NotNull
            Long authorId,
            @NotNull
            String password
    ) {

    }

    @PostMapping()
    public RsData<PostDto> writeItem(
            @RequestBody @Valid PostWriteReqBody reqBody
    ) {
        Member actor = memberService.findById(reqBody.authorId).get();

        //인증체크
        if (!actor.getPassword().equals(reqBody.password)) throw new ServiceException("403-1", "인증에 실패하였습니다.");

        Post post = postService.write(actor, reqBody.title, reqBody.content);

        return new RsData<>(
                        "201-1",
                        "%d번 글이 작성되었습니다.".formatted(post.getId()),
                        new PostDto(post)
               );
    }
}
