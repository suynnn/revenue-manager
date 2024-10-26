package org.streaming.revenuemanagement.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.common.constants.ErrorMessages;
import org.streaming.revenuemanagement.domain.member.entity.Creator;
import org.streaming.revenuemanagement.domain.member.entity.Member;
import org.streaming.revenuemanagement.domain.member.exception.MemberNotFoundException;
import org.streaming.revenuemanagement.domain.member.repository.CreatorRepository;
import org.streaming.revenuemanagement.domain.member.repository.MemberRepository;

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
