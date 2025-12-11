package com.ktai.catcollector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ktai.catcollector.ai.CatJudge;
import com.ktai.catcollector.ai.CatProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@Service
public class CatAiTextService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatClient chat;

    public CatAiTextService(ChatClient.Builder builder) {
        this.chat = builder.build();
    }

    public boolean isCat(String altText, String contextText) {
        String system = """
                あなたは画像メタ情報から、その画像が猫かどうかを判定するアシスタントです。
                与えられた情報が「猫（cat）」が主題である画像だと考えられる場合は {"cat": true}、
                そうでない場合は {"cat": false} のJSONだけを返してください。
                """;

        String user = """
                画像の alt テキスト: %s
                周辺のテキスト: %s
                """.formatted(altText, contextText);

        CatJudge judge = chat.prompt()
                .system(system)
                .user(user)
                .call()
                .entity(CatJudge.class);

        return judge != null && judge.cat();
    }

    public Boolean isCatByImage(byte[] bytes){
        var media = new Media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(bytes));
        var prompt = """
                この画像が猫なら {"cat": true}、
                猫でなければ {"cat": false} のJSONだけ返してください。
                他の説明や文字は禁止です。
                JSONに余計な文字、改行、バッククォート、タグを含めてはいけません。
                "JSON"や```json　のような文字も書いてはいけません。
                 {"cat":true}や {"cat":false}もだめです。一つ空白を入れてください。
                """;
        String content = chat
                .prompt().user(u -> {
                            u.text(prompt);
                            u.media(media);
                        }
                ).call().content();
        log.info(content);
        return content.contains("\"cat\": true");
    }

    public CatProfile generateCatProfile() {
        String system = """
                あなたは猫の名付けと紹介文を考えるアシスタントです。
                日本語で可愛い猫の名前と、その猫の性格や見た目を想像した短いプロフィール、
                そして毛色（白, 黒, 茶トラ, 三毛など）をJSONで出力してください。
                形式: {"name":"...", "profile":"...", "color":"..."}
                """;

        CatProfile profile = chat.prompt()
                .system(system)
                .call()
                .entity(CatProfile.class);

        return profile;
    }

    public CatProfile generateCatProfileFromImage(byte[] imageBytes) throws JsonProcessingException {
        // Vision 用メディアを添付
        var media = new Media(
                MimeTypeUtils.IMAGE_JPEG,
                new ByteArrayResource(imageBytes)
        );
        String prompt = """
        この猫の画像を見てください。
        1. 猫の外見的特徴を30文字以内でprofileに返してください。
        2. 外見に合った「世界に1つだけのユニークな名前を」をnameで返してください。
        3. 猫の毛並みの色の呼称をcolorに返してください。
        4. JSONで必ず出力してください。形式： {"name":"...", "profile":"...", "color":"..."}
        JSONに余計な文字、改行、バッククォート、タグを含めてはいけません。
        "JSON"や```json　のような文字も書いてはいけません。
        """;
        String content = chat
                .prompt()
                .user(u -> u.text(prompt).media(media))  // ← ★画像を見せている
                .call()
                .content();
        log.info(content);
        // パース
        CatProfile result;
        result =objectMapper.readValue(content, CatProfile.class);
        return result;
    }
}