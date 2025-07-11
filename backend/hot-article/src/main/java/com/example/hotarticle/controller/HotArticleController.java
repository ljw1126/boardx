package com.example.hotarticle.controller;

import com.example.hotarticle.service.HotArticleService;
import com.example.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HotArticleController {
    private final HotArticleService hotArticleService;

    @GetMapping("/v1/hot-articles/articles/date/{dateStr}")
    public ResponseEntity<List<HotArticleResponse>> readAll(
            @PathVariable("dateStr") String dateStr
    ) {
        return ResponseEntity.ok(hotArticleService.readAll(dateStr));
    }
}
