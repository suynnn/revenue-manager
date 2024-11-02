package org.revenue.management.member.service;

import lombok.RequiredArgsConstructor;
import org.revenue.management.common.constants.ErrorMessages;
import org.revenue.management.member.entity.Creator;
import org.revenue.management.member.entity.Member;
import org.revenue.management.member.exception.MemberNotFoundException;
import org.revenue.management.member.repository.CreatorRepository;
import org.revenue.management.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreatorService {

    private final CreatorRepository creatorRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void changeToCreator(Long memberId) {

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberNotFoundException(ErrorMessages.MEMBER_NOT_FOUND));

        Creator creator = Creator.builder()
                .member(member)
                .build();

        creatorRepository.save(creator);
    }
}
