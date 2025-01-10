package com.ll.rest.domain.member.member.repository;

import com.ll.rest.domain.member.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
