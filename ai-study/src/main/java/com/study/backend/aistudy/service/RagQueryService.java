package com.study.backend.aistudy.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagQueryService {

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  public RagQueryService(ChatClient chatClient, VectorStore vectorStore) {
    this.chatClient = chatClient;
    this.vectorStore = vectorStore;
  }

  public String query(String question) {
    // 유사도 기반 문서 검색 (Top 5)
    List<Document> relevantDocs = vectorStore.similaritySearch(
        SearchRequest.builder().query(question).topK(5).build()
    );

    String context = relevantDocs.stream()
        .map(Document::getText)
        .collect(Collectors.joining("\n\n"));

    return chatClient.prompt()
        .system("주어진 문서를 기반으로만 답변하세요. 문서에 근거가 없으면 '문서에서 답을 찾을 수 없습니다'라고 답변하세요.")
        .user("문서:\n" + context + "\n\n질문: " + question)
        .call()
        .content();
  }
}
