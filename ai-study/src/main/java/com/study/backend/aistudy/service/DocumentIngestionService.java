package com.study.backend.aistudy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentIngestionService {

  private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

  private final VectorStore vectorStore;

  public DocumentIngestionService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  public int ingest(MultipartFile file) throws IOException {
    Path tempFile = Files.createTempFile("rag-", "-" + file.getOriginalFilename());
    try {
      file.transferTo(tempFile);

      List<Document> chunks = new TokenTextSplitter()
          .apply(new PagePdfDocumentReader(new FileSystemResource(tempFile)).get());

      vectorStore.add(chunks);
      log.info("{}개 청크 적재 완료: {}", chunks.size(), file.getOriginalFilename());
      return chunks.size();
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }
}
