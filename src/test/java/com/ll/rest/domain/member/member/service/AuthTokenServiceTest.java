package com.ll.rest.domain.member.member.service;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.AuthTokenService;
import com.ll.rest.standard.util.Ut;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthTokenServiceTest {
    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private MemberService memberService;

    private int expireSeconds = 60 * 60 * 24 * 365;
    private String secret = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890";

    @Test
    @DisplayName("authTokenService 서비스가 존재한다.")
    void t1() {
        assertThat(authTokenService).isNotNull();
    }

    @Test
    @DisplayName("jjwt 로 JWT 생성, {name=\"Paul\", age=23}")
    void t2() {

        Map<String, Object> payload = Map.of(
                "id", "paul",
                "age", 23
        );

        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes());

        String jwtStr = Jwts.builder()
                .claims(payload)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();

        assertThat(jwtStr).isNotBlank();

        Map<String, Object> parsedPayload = (Map<String, Object>) Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parse(jwtStr)
                .getPayload();

        assertThat(parsedPayload).containsAllEntriesOf(payload);

    }

    @Test
    @DisplayName("")
    void t3() {
        String jwt = Ut.jwt.toString(secret, expireSeconds, Map.of("name", "Paul", "age", "23"));

        assertThat(jwt).isNotBlank();
    }

    @Test
    @DisplayName("authTokenService.genAccessToken(member)")
    void t4() {
        Member member = memberService.findByUsername("user1").get();

        String accessToken = authTokenService.genAccessToken(member);

        assertThat(accessToken).isNotBlank();
    }
}