package com.ll.rest.domain.post.post.service;

import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.domain.post.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

}
