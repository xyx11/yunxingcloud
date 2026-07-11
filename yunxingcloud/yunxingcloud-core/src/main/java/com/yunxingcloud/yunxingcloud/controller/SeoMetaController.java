package com.yunxingcloud.yunxingcloud.controller;

import com.yunxingcloud.common.core.SeoMetaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "SEO元数据", description = "SEO元数据管理")
@RestController
@RequestMapping("/api/seo")
public class SeoMetaController {

    @GetMapping(value = "/meta/product/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> productMeta(@PathVariable Long id,
                                          @RequestParam String name,
                                          @RequestParam(required = false) String desc,
                                          @RequestParam(required = false) String img,
                                          @RequestParam String url) {
        return ResponseEntity.ok(SeoMetaService.productPage(name, desc, img, url));
    }

    @GetMapping("/meta/home")
    public ResponseEntity<?> homeMeta() {
        return ResponseEntity.ok(SeoMetaService.homePage());
    }

    @GetMapping("/sitemap")
    public ResponseEntity<String> sitemap() {
        String base = "https://www.yunxingcloud.com";
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
              <url><loc>%s/</loc><priority>1.0</priority></url>
              <url><loc>%s/products</loc><priority>0.8</priority></url>
              <url><loc>%s/mall/</loc><priority>0.9</priority></url>
            </urlset>""".formatted(base, base, base);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml);
    }
}