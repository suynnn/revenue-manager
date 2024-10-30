package org.streaming.revenuemanagement.common.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.streaming.revenuemanagement.domain.member.service.CreatorService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/creator")
public class CreatorApiController {

    private final CreatorService creatorService;

    @PostMapping("/change/{memberId}")
    public ResponseEntity<Void> creatorChange(@PathVariable("memberId") Long memberId) {

        creatorService.changeToCreator(memberId);

        return ResponseEntity.ok().build();
    }
}
