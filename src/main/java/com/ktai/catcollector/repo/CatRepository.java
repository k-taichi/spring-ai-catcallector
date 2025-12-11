package com.ktai.catcollector.repo;

import com.ktai.catcollector.model.Cat;
import com.ktai.catcollector.model.CatImage;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CatRepository {

    private final Map<Long, Cat> cats = new LinkedHashMap<>();
    private final Set<String> imageHashes = new HashSet<>();
    private final Set<String> urls = new HashSet<>();
    private final AtomicLong idSeq = new AtomicLong(1);

    public synchronized Optional<Cat> findById(long id) {
        return Optional.ofNullable(cats.get(id));
    }

    public synchronized List<Cat> findAll() {
        return new ArrayList<>(cats.values());
    }

    public synchronized boolean existsUrls(String url) {
        return urls.contains(url);
    }

    public synchronized boolean existsImageHash(String hash) {
        return imageHashes.contains(hash);
    }

    public synchronized void addImageHash(String hash) {
        imageHashes.add(hash);
    }

    public synchronized Cat createCat(String firstSourceUrl) {
        long id = idSeq.getAndIncrement();
        String slug = "cat-%03d".formatted(id);
        Cat cat = new Cat(id, slug, firstSourceUrl);
        cats.put(id, cat);
        return cat;
    }

    public synchronized void setCatProfile(Cat cat, String name, String profile, String color) {
        cat.setName(name);
        cat.setProfile(profile);
        cat.setColor(color);
    }

    public synchronized void addImageToCat(Cat cat, String imageUrl, String localPath, String hash) {
        cat.getImages().add(new CatImage(imageUrl, localPath, hash, LocalDateTime.now()));
    }
}