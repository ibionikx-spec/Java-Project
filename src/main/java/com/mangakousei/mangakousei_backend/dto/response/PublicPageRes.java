package com.mangakousei.mangakousei_backend.dto.response;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PublicPageRes {
    private Long pageId;
    private int pageNumber;
    private String fileUrl;
}
