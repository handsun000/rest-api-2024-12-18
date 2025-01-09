package com.ll.rest.domain.post.post.controller;

import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.service.PostService;
import com.ll.rest.global.rsData.RsData;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class ApiV1PostController {

    private final PostService postService;

    @GetMapping
    public List<Post> getItems() {
        return postService.findAllByOrderByIdDesc();
    }

    @GetMapping("/{id}")
    public Post getItem(
            @PathVariable long id
    ) {
        return postService.findById(id).get();
    }

    @DeleteMapping("/{id}")
    public RsData deleteItem(
            @PathVariable long id
    ) {
        Post post = postService.findById(id).get();

        postService.delete(post);

        return new RsData(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(id)
        );
    }

    record PostModifyReqBody(
            String title,
            String content
    ) {

    }

    @PutMapping("/{id}")
    @Transactional
    public RsData modifyItem(
            @PathVariable long id,
            @RequestBody PostModifyReqBody reqBody
    ) {
        Post post = postService.findById(id).get();

        postService.modify(post, reqBody.title, reqBody.content);

        return new RsData(
                "200-1",
                "%d번 글이 수정되었습니다.".formatted(id)
        );
    }
}
