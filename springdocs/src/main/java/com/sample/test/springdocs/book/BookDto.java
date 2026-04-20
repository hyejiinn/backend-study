package com.sample.test.springdocs.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * Book 도메인 DTO 모음
 *
 * [개념] Java record
 *   - 불변(immutable) 데이터 전달 객체를 간결하게 선언하는 Java 16+ 문법
 *   - getter, equals, hashCode, toString 자동 생성
 *   - Spring REST Docs의 requestFields/responseFields 문서화 대상
 */
public class BookDto {

  // ── 등록 요청 DTO ──────────────────────────────────────────────
  public record CreateRequest(
      @NotBlank String title,      // 책 제목 (필수)
      @NotBlank String author,     // 저자 (필수)
      @Positive int price          // 가격 (양수)
  ) {}

  // ── 수정 요청 DTO ──────────────────────────────────────────────
  public record UpdateRequest(
      @NotBlank String title,
      @NotBlank String author,
      @Positive int price
  ) {}

  // ── 단건 응답 DTO ──────────────────────────────────────────────
  public record BookResponse(
      Long id,
      String title,
      String author,
      int price
  ) {}

  // ── 목록 응답 DTO ──────────────────────────────────────────────
  // [개념] REST Docs에서 배열 필드는 books[].id 형식으로 문서화
  public record BookListResponse(
      int totalCount,
      List<BookResponse> books
  ) {}
}
