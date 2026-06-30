package com.study.backend.aistudy.controller;

import com.study.backend.aistudy.dto.QueryRequest;
import com.study.backend.aistudy.dto.QueryResponse;
import com.study.backend.aistudy.service.RagQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RagController {

  private final RagQueryService ragQueryService;

  public RagController(RagQueryService ragQueryService) {
    this.ragQueryService = ragQueryService;
  }

  @PostMapping("/query")
  public ResponseEntity<QueryResponse> query(@RequestBody QueryRequest request) {
    if (request.question() == null || request.question().isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    String answer = ragQueryService.query(request.question());
    return ResponseEntity.ok(new QueryResponse(answer));
  }
}
