package com.example.concurrencystudy.service;

import com.example.concurrencystudy.entity.Stock;
import com.example.concurrencystudy.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 기본 Service (락 X), 동시성 이슈 문제 발생 🚨
 */
@Service
@RequiredArgsConstructor
public class PlainService {
    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long id, int amount) {
        Stock stock = stockRepository.findById(id).orElseThrow();

        if (stock.getQty() < amount) throw new IllegalStateException("재고 부족");

        stock.setQty(stock.getQty() - amount);
    }
}
