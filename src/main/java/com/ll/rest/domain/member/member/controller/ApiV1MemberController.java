package com.ll.rest.domain.member.member.controller;

import com.ll.rest.domain.member.member.dto.MemberDto;
import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.AuthTokenService;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rq.Rq;
import com.ll.rest.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class ApiV1MemberController {

    private final MemberService memberService;
    private final Rq rq;

    record MemberJoinReqBody(
            @NotBlank
            String username,
            @NotBlank
            String password,
            @NotBlank
            String nickname
    ) {
    }

    @PostMapping("/join")
    public RsData<MemberDto> join(@RequestBody @Valid MemberJoinReqBody reqBody) {
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);

        return new RsData<>("201-1",
                "%s님 환영합니다. 회원가입이 완료되었습니다.".formatted(member.getNickname()),
                new MemberDto(member)
        );
    }

    record MemberLoginReqBody(
            @NotBlank
            String username,
            @NotBlank
            String password
    ) {
    }

    record MemberLoginResBody(
            MemberDto item,
            String apiKey,
            String accessToken
    ) {
    }

    @PostMapping("/login")
    public RsData<MemberLoginResBody> login(@RequestBody @Valid MemberLoginReqBody reqBody) {
        Member member = memberService.findByUsername(reqBody.username)
                .orElseThrow(() -> new ServiceException("401-1", "존재하지 않는 사용자 입니다."));

        if (!member.matchPassword(reqBody.password))
            throw new ServiceException("401-2", "비밀번호가 맞지 않습니다.");

        String accessToken = memberService.genAccessToken(member);

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("apiKey", member.getApiKey());

        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getNickname()),
                new MemberLoginResBody(
                        new MemberDto(member),
                        member.getApiKey(),
                        accessToken
                )
        );
    }

    @GetMapping("/me")
    public MemberDto me() {

        Member member = rq.findByActor().get();


        return new MemberDto(member);
    }
}