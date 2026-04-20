package com.sample.test.springdocs.book;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Book 서비스
 *
 * [개념] 스터디 편의상 인메모리 ConcurrentHashMap 저장소를 사용.
 *        실제 프로젝트에서는 JPA Repository 또는 MyBatis Mapper로 교체.
 */
@Service
public class BookService {

  private final Map<Long, BookDto.BookResponse> store = new ConcurrentHashMap<>();
  private final AtomicLong sequence = new AtomicLong(1L);

  // 초기 샘플 데이터
  public BookService() {
    store.put(1L, new BookDto.BookResponse(1L, "Clean Code", "Robert C. Martin", 35000));
    store.put(2L, new BookDto.BookResponse(2L, "Effective Java", "Joshua Bloch", 42000));
    sequence.set(3L);
  }

  public BookDto.BookResponse findById(Long id) {
    BookDto.BookResponse book = store.get(id);
    if (book == null) {
      throw new BookNotFoundException(id);
    }
    return book;
  }

  public BookDto.BookListResponse findAll() {
    List<BookDto.BookResponse> books = new ArrayList<>(store.values());
    return new BookDto.BookListResponse(books.size(), books);
  }

  public BookDto.BookResponse create(BookDto.CreateRequest request) {
    Long id = sequence.getAndIncrement();
    BookDto.BookResponse book = new BookDto.BookResponse(
        id, request.title(), request.author(), request.price());
    store.put(id, book);
    return book;
  }

  public BookDto.BookResponse update(Long id, BookDto.UpdateRequest request) {
    findById(id); // 존재 여부 확인
    BookDto.BookResponse updated = new BookDto.BookResponse(
        id, request.title(), request.author(), request.price());
    store.put(id, updated);
    return updated;
  }

  public void delete(Long id) {
    findById(id); // 존재 여부 확인
    store.remove(id);
  }

  // ── 예외 클래스 ────────────────────────────────────────────────
  public static class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
      super("Book not found: " + id);
    }
  }
}
