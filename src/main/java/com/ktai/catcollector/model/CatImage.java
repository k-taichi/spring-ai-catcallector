package com.ktai.catcollector.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CatImage {
    private final String imageUrl;
    private final String localPath;
    private final String hash;
    private final LocalDateTime foundAt;

    public CatImage(String imageUrl, String localPath, String hash, LocalDateTime foundAt) {
        this.imageUrl = imageUrl;
        this.localPath = localPath;
        this.hash = hash;
        this.foundAt = foundAt;
    }
}