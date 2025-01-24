package com.ll.rest.domain.post.post.entity;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.post.comment.entity.PostComment;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.jpa.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseTime {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member author;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean published;

    private boolean listed;

    @OneToMany(mappedBy = "post", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    public PostComment addComment(Member author, String content) {
        PostComment comment = PostComment.builder()
                .post(this)
                .author(author)
                .content(content)
                .build();

        comments.add(comment);

        return comment;
    }

    public List<PostComment> getCommentsByOrderByIdDesc() {
        return comments.reversed();
    }

    public Optional<PostComment> getCommentById(long id) {
        return comments.stream()
                .filter(postComment -> postComment.getId() == id)
                .findFirst();
    }

    public void removeComment(PostComment comment) {
        comments.remove(comment);
    }

    public void checkActorCanDelete(Member actor) {
        if (actor == null) throw new ServiceException("401-1", "로그인 후 이용해주세요");

        if (actor.isAdmin() || actor.equals(author)) return;

        throw new ServiceException("403-1", "작성자만 글을 삭제할 권한이 있습니다.");
    }

    public void checkActorCanModify(Member actor) {
        if (actor == null) throw new ServiceException("403-1", "로그인 후 이용해주세요");

        if (actor.isAdmin() || actor.equals(author)) return;

        throw new ServiceException("403-1", "작성자만 글을 수정할 권한이 있습니다.");
    }

    public void modify(String title, String content) {
        setTitle(title);
        setContent(content);
    }

    public void checkActorCanRead(Member actor) {
        if (actor == null) throw new ServiceException("401-1", "로그인 후 이용해주세요");

        if (actor.isAdmin() || actor.equals(author)) return;

        throw new ServiceException("403-1", "비공개글은 작성자만 볼 수 있습니다.");
    }
}