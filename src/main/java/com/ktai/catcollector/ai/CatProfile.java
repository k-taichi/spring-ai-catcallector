package com.ktai.catcollector.ai;

// 猫の名前 & プロフィール生成
public record CatProfile(
    String name,
    String profile,
    String color
) {}