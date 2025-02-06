package com.ll.rest.global.rq;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.exception.ServiceException;
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

    public Member getActor() {
        SecurityContext context = SecurityContextHolder.getContext();

        Authentication authentication = context.getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null || authentication.getPrincipal() instanceof String) return null;

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username = user.getUsername();
        return memberService.findByUsername(username).get();
    }
}
