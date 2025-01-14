package com.ll.rest.domain.member.member.controller;

import com.ll.rest.domain.member.member.dto.MemberDto;
import com.ll.rest.domain.member.member.entity.Member;
import com.ll.rest.domain.member.member.service.MemberService;
import com.ll.rest.global.exception.ServiceException;
import com.ll.rest.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class ApiV1MemberController {
    private final MemberService memberService;

    record MemberJoinReqBody(
            @NotBlank
            @Length(min = 4)
            String username,
            @NotBlank
            @Length(min = 4)
            String password,
            @NotBlank
            @Length(min = 2)
            String nickname
    ) {

    }

    @PostMapping("/join")
    public RsData<MemberDto> join(
            @RequestBody @Valid MemberJoinReqBody reqBody
    ) {
        Member member = memberService.join(reqBody.username, reqBody.password, reqBody.nickname);
        return new RsData<>(
                "201-1",
                "%s님 환영합니다".formatted(member.getNickname()),
                new MemberDto(member)
        );
    }

    record MemberLoginResBody(
            @NotBlank
            @Length(min = 4)
            String username,
            @NotBlank
            @Length(min = 4)
            String password
    ) {
    }

    record MemberLoginReqBody(
            MemberDto item,
            String apiKey
    ) {
    }

    @PostMapping("/login")
    public RsData<MemberLoginReqBody> login(
            @RequestBody @Valid MemberLoginResBody reqBody
    ) {
        Member member = memberService.findByUsername(reqBody.username)
                .orElseThrow(() -> new ServiceException("401-1", "해당 회원은 존재하지 않습니다."));

        if (!member.getPassword().equals(reqBody.password)) {
            throw new ServiceException("401-2", "비밀번호가 일치하지 않습니다.");
        }

        return new RsData<>(
                "200-1",
                "%s님 환영합니다".formatted(member.getNickname()),
                new MemberLoginReqBody(
                        new MemberDto(member),
                        member.getApiKey()
                )
        );
    }
}
