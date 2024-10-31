package org.streaming.revenuemanagement.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.streaming.revenuemanagement.common.oauth2.userinfo.CustomOAuth2User;
import org.streaming.revenuemanagement.domain.advertisement.dto.AdvertisementRespDto;
import org.streaming.revenuemanagement.domain.advertisement.service.AdvertisementService;
import org.streaming.revenuemanagement.domain.video.service.VideoService;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogRespDto;
import org.streaming.revenuemanagement.domain.videolog.service.VideoLogService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final VideoLogService videoLogService;
    private final VideoService videoService;
    private final AdvertisementService advertisementService;

    @GetMapping("/")
    public String index() {

        return "index";
    }

    @GetMapping("/video/{videoId}")
    public String videoPage(@PathVariable("videoId") Long videoId, Model model) {
        model.addAttribute("videoRespDto", videoService.findById(videoId));
        model.addAttribute("videoId", videoId);

        List<AdvertisementRespDto> advertisementRespDtoList = advertisementService.findAll();
        model.addAttribute("advertisementRespDtoList", advertisementRespDtoList);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomOAuth2User) {

            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            Long memberId = customOAuth2User.getId();
            model.addAttribute("memberId", memberId);

            VideoLogRespDto videoLogRespDto = videoLogService.findFirstVideoLogByVideoIdAndMemberId(videoId, memberId);
            if (videoLogRespDto != null) {
                model.addAttribute("videoLogRespDto", videoLogRespDto);
            }
        }

        return "streaming";
    }

}
