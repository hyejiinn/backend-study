package com.study.backend.aistudy.controller;

import com.study.backend.aistudy.service.DocumentIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class IngestController {

  private final DocumentIngestionService documentIngestionService;

  public IngestController(DocumentIngestionService documentIngestionService) {
    this.documentIngestionService = documentIngestionService;
  }

  @PostMapping("/ingest")
  public ResponseEntity<Map<String, Object>> ingest(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
    }
    try {
      int chunkCount = documentIngestionService.ingest(file);
      return ResponseEntity.ok(Map.of(
          "message", "문서 적재 완료",
          "filename", file.getOriginalFilename(),
          "chunkCount", chunkCount
      ));
    } catch (IOException e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "파일 처리 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }
}
