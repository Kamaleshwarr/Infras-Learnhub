package com.company.learninghub.common.pagination;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<SortItem> sort
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSort().stream()
                        .map(SortItem::from)
                        .toList()
        );
    }

    public record SortItem(
            String property,
            String direction
    ) {
        private static SortItem from(Sort.Order order) {
            return new SortItem(order.getProperty(), order.getDirection().name());
        }
    }
}

