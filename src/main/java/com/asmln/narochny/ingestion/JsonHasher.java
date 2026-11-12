package com.asmln.narochny.ingestion;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class JsonHasher {
    // Создаем специальный mapper для нормализации (потокобезопасный, можно сделать полем класса/бином)
    private static final ObjectMapper CANONICAL_MAPPER = JsonMapper.builder()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .build();

    public static String calculateSha256(String rawJson) throws Exception {
        // 1. Парсим исходный JSON в дерево (заодно проверяется валидность)
        JsonNode jsonNode = CANONICAL_MAPPER.readTree(rawJson);

        // 2. Сериализуем обратно в компактную строку без пробелов с отсортированными ключами
        String normalizedJson = CANONICAL_MAPPER.writeValueAsString(jsonNode);

        // 3. Считаем SHA-256 и переводим в hex-строку (Java 17+)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(normalizedJson.getBytes(StandardCharsets.UTF_8));

        return HexFormat.of().formatHex(hashBytes);
    }
}
