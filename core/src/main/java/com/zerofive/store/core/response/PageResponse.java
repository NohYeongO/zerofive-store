package com.zerofive.store.core.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "페이지네이션 응답")
public record PageResponse<T>(
        @Schema(description = "데이터 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 요소 수", example = "100")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages
) {
}
