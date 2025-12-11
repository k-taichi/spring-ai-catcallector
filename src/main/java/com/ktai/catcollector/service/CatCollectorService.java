package com.ktai.catcollector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ktai.catcollector.ai.CatProfile;
import com.ktai.catcollector.model.Cat;
import com.ktai.catcollector.props.TargetUrls;
import com.ktai.catcollector.props.TargetUrlsProps;
import com.ktai.catcollector.repo.CatRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.List;

@Slf4j
@Service
public class CatCollectorService {

    private final CatAiTextService catAiService;
    private final CatRepository catRepository;
    private final TargetUrlsProps targetUrlsProps;
    private final Path outputDir;

    public CatCollectorService(
            CatAiTextService catAiService,
            CatRepository catRepository,
            TargetUrlsProps targetUrls,
            @Value("${cat-collector.output-dir}") String outputDir
    ) throws IOException {
        this.catAiService = catAiService;
        this.catRepository = catRepository;
        this.targetUrlsProps = targetUrls;
        this.outputDir = Paths.get(outputDir);
        Files.createDirectories(this.outputDir);
        Files.createDirectories(this.outputDir.resolve("images"));
        Files.createDirectories(this.outputDir.resolve("pages"));
    }

    public void crawlAndCollect() {
        for (TargetUrls targetUrls : targetUrlsProps.getTargetUrls()) {
            if(targetUrls.isCrawled()){
                continue;
            }
            try {
                targetUrls.setCrawled(crawlSite(targetUrls.getUrl()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.error("Crawling failed, url: " + targetUrls.getUrl());
            }
        }
        generateSite();
        log.info("Crawling finished");
    }

    private boolean crawlSite(String url) throws IOException, InterruptedException {
        boolean result = true;
        Document doc = Jsoup.connect(url).get();
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.absUrl("src");
            if (src == null || src.isBlank()) continue;

            String alt = img.attr("alt");
            String context = doc.title();
            String hash;
            byte[] bytes;
            // 画像保存 & ハッシュ
            try (InputStream in = Jsoup.connect(src).ignoreContentType(true).execute().bodyStream()) {
                bytes = in.readAllBytes();
                hash = hashImage(bytes);
            } catch (Exception e) {
                System.err.println("Failed to download image: " + src + " : " + e.getMessage());
                continue;
            }
            if (hash == null) continue;
            synchronized (catRepository) {
                if (catRepository.existsImageHash(hash)) {
                    // 既に取り込み済み
                    log.warn("Image hash already exists: " + hash);
                    continue;
                }
                catRepository.addImageHash(hash);
                result = false;
                // ここでAI判定
                Thread.sleep(5000);
                if (!catAiService.isCat(alt, context)) {
                    continue;
                }
                //厳密猫判定
                Boolean isCat=catAiService.isCatByImage(bytes);
                if (isCat==null||!isCat) {
                    continue;
                }

                // とりあえず「1枚画像＝1匹の猫」で新規Catを作る
                Cat cat = catRepository.createCat(url);
                CatProfile profile;
                // プロフィール生成
                //try {
                    Thread.sleep(10000);
                    profile = catAiService.generateCatProfileFromImage(bytes);
                //}catch (JsonProcessingException e) {
                //    profile = catAiService.generateCatProfile();
                //}

                catRepository.setCatProfile(cat, profile.name(), profile.profile(), profile.color());

                // 画像関連づけ
                String fileName = hash + ".jpg";
                String localPath = "images/" + fileName;
                catRepository.addImageToCat(cat, src, localPath, hash);
            }
        }
        return result;
    }

    private String hashImage(byte[] bytes) throws IOException {
            String hash = DigestUtils.md5DigestAsHex(bytes);
            Path file = outputDir.resolve("images").resolve(hash + ".jpg");
            if (!Files.exists(file)) {
                Files.write(file, bytes, StandardOpenOption.CREATE_NEW);
            }
            return hash;
    }

    private void generateSite() {
        var cats = catRepository.findAll();
        try {
            generateIndexPage(cats);
            for (Cat cat : cats) {
                generateCatPage(cat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateIndexPage(List<Cat> cats) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                  <meta charset="UTF-8">
                  <title>猫コレクション</title>
                </head>
                <body>
                <h1>猫コレクション</h1>
                <ul>
                """);

        for (Cat c : cats) {
            sb.append("""
                    <li><a href="cats/%s.html">%s（%s）</a></li>
                    """.formatted(c.getSlug(), safe(c.getName()), safe(c.getColor())));
        }

        sb.append("""
                </ul>
                </body>
                </html>
                """);

        Path index = outputDir.resolve("index.html");
        Files.writeString(index, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void generateCatPage(Cat cat) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                  <meta charset="UTF-8">
                  <title>%s - 猫コレクション</title>
                </head>
                <body>
                <h1>%s</h1>
                <p>%s</p>
                <p>毛色: %s</p>
                <h2>写真一覧</h2>
                <div>
                """.formatted(
                safe(cat.getName()), safe(cat.getName()),
                safe(cat.getProfile()), safe(cat.getColor())
        ));

        for (var img : cat.getImages()) {
            sb.append("""
                    <img src="../%s" style="max-width:300px; margin:8px;">
                    """.formatted(img.getLocalPath()));
        }

        sb.append("""
                </div>
                <p><a href="../index.html">猫一覧に戻る</a></p>
                </body>
                </html>
                """);

        Path pageDir = outputDir.resolve("cats");
        Files.createDirectories(pageDir);
        Path page = pageDir.resolve(cat.getSlug() + ".html");
        Files.writeString(page, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}