package com.ll.rest.domain.member.member.service;

import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;
    @Value("${custom.accessToken.expirationSec}")
    private long accessTokenExpirationSec;

    public String genAccessToken(Member member) {
        long id = member.getId();
        String username = member.getUsername();

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSec,
                Map.of("id", id, "username", username)
        );
    }

    public Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if (parsedPayload == null) return null;

        long id = (long) (Integer) parsedPayload.get("id");
        String username = (String) parsedPayload.get("username");

        return Map.of("id", id, "username", username);
    }
}
