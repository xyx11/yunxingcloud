package com.yunxingcloud.order.controller;

import com.yunxingcloud.order.service.SearchSuggestService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchSuggestController {

    private final SearchSuggestService searchSuggestService;

    public SearchSuggestController(SearchSuggestService searchSuggestService) {
        this.searchSuggestService = searchSuggestService;
    }

    @GetMapping("/suggest")
    public List<Map<String, Object>> suggest(@RequestParam String q, @RequestParam(defaultValue = "5") int limit) {
        return searchSuggestService.suggest(q, limit);
    }

    @GetMapping("/hot-keywords")
    public List<String> hotKeywords() {
        return searchSuggestService.hotKeywords();
    }

    @PostMapping("/log")
    public Map<String, Object> logSearch(@RequestBody Map<String, String> body) {
        searchSuggestService.logSearch(body.get("keyword"));
        return Map.of("success", true);
    }

    @GetMapping("/complete")
    public List<Map<String, Object>> complete(@RequestParam String q) {
        return searchSuggestService.complete(q);
    }
}
