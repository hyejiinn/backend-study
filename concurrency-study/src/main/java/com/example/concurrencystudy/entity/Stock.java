package com.example.concurrencystudy.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Column(nullable = false)
    private int qty; // 현재 재고 개수

    @Version
    private int version; // 낙관적 락 필드
}
