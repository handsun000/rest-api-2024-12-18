package com.ll.rest.global.rq;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.standard.util.Ut;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;
import java.util.Optional;

@RequestScope
@Component
@RequiredArgsConstructor
public class Rq {

    private final HttpServletRequest request;
    private final MemberService memberService;

    public Member checkAuthentication() {
        String credentials = request.getHeader("Authorization");

        String apiKey = credentials == null ? "" : credentials.substring("Bearer ".length());

        if (Ut.str.isBlank(apiKey)) {
            throw new ServiceException("401-1", "인증정보가 없습니다.");
        }

        Optional<Member> opActor = memberService.findByApiKey(apiKey);

        if (opActor.isEmpty())
            throw new ServiceException("401-1", "사용자 인증정보가 올바르지 않습니다.");

        return opActor.get();
    }

    public Member getActorByUsername(String username) {

        return memberService.findByUsername(username)
                .orElseThrow(() -> new ServiceException("404-1", "사용자를 찾을 수 없습니다."));
    }

    public void setLogin(String username) {
        UserDetails user = new User(
                username,
                "",
                List.of()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
