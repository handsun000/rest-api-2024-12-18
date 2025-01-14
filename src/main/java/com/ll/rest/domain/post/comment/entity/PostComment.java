package com.ll.rest.domain.post.comment.entity;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.post.post.entity.Post;
import com.ll.rest.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostComment extends BaseTime {

    @ManyToOne
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @Column(columnDefinition = "TEXT")
    private String content;
}
