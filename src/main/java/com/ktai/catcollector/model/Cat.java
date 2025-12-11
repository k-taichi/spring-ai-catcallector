package com.ktai.catcollector.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cat {
    private final long id;
    private final String slug;
    private String name;
    private String profile;
    private String color;
    private final String firstSourceUrl;
    private final List<CatImage> images = new ArrayList<>();

    public Cat(long id, String slug, String firstSourceUrl) {
        this.id = id;
        this.slug = slug;
        this.firstSourceUrl = firstSourceUrl;
    }

}