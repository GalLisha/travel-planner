package com.travelplanner.util;

import com.travelplanner.http.JsonUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Minimal hand-rolled signed token (HMAC-SHA256), avoiding a JWT library dependency -
 * consistent with this codebase's preference for hand-rolling small infrastructure
 * pieces (see SimpleHttpClient) over pulling in libraries. Format:
 * base64url(payloadJson) + "." + base64url(HMAC-SHA256 signature).
 */
public final class TokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long EXPIRY_MILLIS = 7L * 24 * 60 * 60 * 1000; // 7 days

    private final byte[] secret;

    public TokenService(String configuredSecret) {
        this.secret = (configuredSecret != null && !configuredSecret.trim().isEmpty())
                ? configuredSecret.trim().getBytes(StandardCharsets.UTF_8)
                : randomSecret();
    }

    public String issue(String userId, String email) {
        Claims claims = new Claims();
        claims.userId = userId;
        claims.email = email;
        claims.exp = System.currentTimeMillis() + EXPIRY_MILLIS;

        String payload = base64Url(JsonUtil.GSON.toJson(claims).getBytes(StandardCharsets.UTF_8));
        String signature = base64Url(sign(payload));
        return payload + "." + signature;
    }

    /** Verifies signature + expiry and returns the embedded claims, or throws if invalid/expired. */
    public Claims verify(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new SecurityException("Missing token");
        }
        int dot = token.indexOf('.');
        if (dot < 0) {
            throw new SecurityException("Malformed token");
        }
        String payload = token.substring(0, dot);
        String signature = token.substring(dot + 1);

        String expectedSignature = base64Url(sign(payload));
        if (!expectedSignature.equals(signature)) {
            throw new SecurityException("Invalid token signature");
        }

        Claims claims = JsonUtil.GSON.fromJson(
                new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8), Claims.class);
        if (claims.exp < System.currentTimeMillis()) {
            throw new SecurityException("Token expired");
        }
        return claims;
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign token", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] randomSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static final class Claims {
        public String userId;
        public String email;
        public long exp;
    }
}
