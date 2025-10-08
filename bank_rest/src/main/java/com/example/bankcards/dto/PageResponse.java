package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с пагинацией")
public class PageResponse<T> {

    @Schema(description = "Список элементов на текущей странице")
    private List<T> content;

    @Schema(description = "Номер текущей страницы", example = "0")
    private int currentPage;

    @Schema(description = "Размер страницы", example = "20")
    private int pageSize;

    @Schema(description = "Общее количество элементов", example = "150")
    private long totalElements;

    @Schema(description = "Общее количество страниц", example = "8")
    private int totalPages;

    @Schema(description = "Есть ли следующая страница", example = "true")
    private boolean hasNext;

    @Schema(description = "Есть ли предыдущая страница", example = "false")
    private boolean hasPrevious;

    public PageResponse(List<T> content, int currentPage, int pageSize, long totalElements) {
        this.content = content;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.hasNext = currentPage < totalPages - 1;
        this.hasPrevious = currentPage > 0;
    }
}
