package org.revenue.management.member.controller;

import lombok.RequiredArgsConstructor;
import org.revenue.management.member.service.CreatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
