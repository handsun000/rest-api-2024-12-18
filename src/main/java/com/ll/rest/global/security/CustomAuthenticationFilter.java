package com.ll.rest.global.security;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final Rq rq;
    private final MemberService memberService;

    record AuthTokens(String apiKey, String accessToken) {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (List.of("/api/v1/members/login", "/api/v1/members/logout", "/api/v1/members/join").contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<AuthTokens> tokens = extractTokens(request);
        if (tokens.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthTokens authTokens = tokens.get();
        Member member = authenticateMember(authTokens);

        if (member != null) rq.setLogin(member);

        filterChain.doFilter(request, response);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private Optional<AuthTokens> extractTokens(HttpServletRequest request) {
        String apiKey = null;
        String accessToken = null;

        String authorization = rq.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String[] tokenBits = authorization.substring("Bearer ".length()).split(" ", 2);
            if (tokenBits.length == 2) {
                apiKey = tokenBits[0];
                accessToken = tokenBits[1];
            }
        }

        if (apiKey == null || accessToken == null) {
            apiKey = rq.getCookieValue("apikey");
            accessToken = rq.getCookieValue("accessToken");
        }

        return (apiKey != null && accessToken != null) ?
                Optional.of(new AuthTokens(apiKey, accessToken)) : Optional.empty();
    }

    private Member authenticateMember(AuthTokens tokens) {
        Member member = memberService.getMemberFromAccessToken(tokens.accessToken);

        if (member == null) {
            Optional<Member> opMemberByApiKey = memberService.findByApiKey(tokens.apiKey);
            if (opMemberByApiKey.isPresent()) {
                member = opMemberByApiKey.get();
                updateAuthorizationHeader(tokens.apiKey, member);
            }
        }

        return member;
    }

    private void updateAuthorizationHeader(String apiKey, Member member) {
        String newAccessToken = memberService.genAccessToken(member);

        rq.setHeader("Authorization", "Bearer " + apiKey + " " + newAccessToken);
        rq.setCookie("accessToken", newAccessToken);
    }
}
