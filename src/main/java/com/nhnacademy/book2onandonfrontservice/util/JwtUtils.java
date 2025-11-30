package com.nhnacademy.book2onandonfrontservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

public class JwtUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    //JWT에서 userId추출 (SecretKey빼고 디코딩)
    public static Long getUserId(String token) {
        try {
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String payload = new String(decoder.decode(chunks[1]));

            JsonNode node = objectMapper.readTree(payload);

            return node.get("userId").asLong();

        } catch (Exception e) {
            return null;
        }
    }
}
