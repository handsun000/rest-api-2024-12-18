package com.ll.rest.domain.member.member.service;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.standard.util.Ut;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {
    public String genAccessToken(Member member) {
        long id = member.getId();
        String username = member.getUsername();

        return Ut.jwt.toString(
                "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz1234567890",
                60 * 60 * 24 * 365,
                Map.of("id", id, "username", username)
        );
    }
}
