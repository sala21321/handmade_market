// src/main/java/com/example/handmademarket/dto/SearchResultDTO.java
package com.example.handmademarket.dto;

import com.example.handmademarket.entity.Goods;
import lombok.Data;

import java.util.List;

@Data
public class SearchResultDTO {
    private List<Goods> goodsList;
    private Long total;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
}