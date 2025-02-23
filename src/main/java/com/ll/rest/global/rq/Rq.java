package com.ll.rest.global.rq;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.security.SecurityUser;
import com.ll.rest.standard.util.Ut;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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

    public void setLogin(Member member) {
        UserDetails user = new SecurityUser(
                member.getId(),
                member.getUsername(),
                "",
                member.getAuthorities()
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                user.getPassword(),
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Member getActor() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                )
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof SecurityUser)
                .map(principal -> (SecurityUser) principal)
                .map(securityUser -> new Member(securityUser.getId(), securityUser.getUsername()))
                .orElse(null);
    }
}
