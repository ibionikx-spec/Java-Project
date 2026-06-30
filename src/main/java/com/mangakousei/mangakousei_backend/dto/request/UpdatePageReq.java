package com.mangakousei.mangakousei_backend.dto.request;

import lombok.Data;

@Data
public class UpdatePageReq {
    private String fileUrl;
    private Integer pageNumber;
}