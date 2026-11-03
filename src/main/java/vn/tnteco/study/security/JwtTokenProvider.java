package vn.tnteco.study.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Lớp tiện ích quản lý vòng đời JSON Web Token (JWT).
 * Chịu trách nhiệm ký sinh token, giải mã Claims và kiểm tra tính hợp lệ của token.
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Value("${jwt.valid-duration}")
    private long validDuration;

    /**
     * Đoạn này làm gì: Tạo khóa bí mật HMAC-SHA từ chuỗi signerKey cấu hình trong application.yaml.
     * Tại sao làm vậy: JJWT 0.12.x yêu cầu SecretKey an toàn đạt chuẩn độ dài tối thiểu cho thuật toán HS256/HS512.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(signerKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SINH ACCESS TOKEN CHỨA CLAIM AUTHORITIES:
     * Đoạn này làm gì:
     * - Nhận username, userId và danh sách mã quyền hạn (permissions) chi tiết.
     * - Nạp danh sách quyền hạn trực tiếp vào Claim "authorities" của Token.
     * Tại sao làm vậy:
     * - Đáp ứng kiến trúc Fine-grained RBAC: Tách rời quyền khỏi tên Role thô.
     * - Giúp Filter tầng sau đọc trực tiếp quyền mà không cần query lại CSDL.
     */
    public String generateToken(String username, String userId, Collection<String> authorities) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validDuration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                // Lưu thẳng danh sách permissions (ví dụ: ["product:read", "product:create"]) vào claim authorities
                .claim("authorities", authorities)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Đoạn này làm gì: Giải mã và đọc toàn bộ Payload (Claims) từ chuỗi Token đã ký.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Trích xuất username từ subject của token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Trích xuất userId từ payload của token
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * TRÍCH XUẤT DANH SÁCH QUYỀN HẠN (AUTHORITIES):
     * Đoạn này làm gì:
     * - Đọc Claim "authorities" dưới dạng List<String>.
     * - Nếu token cũ chứa claim "roles", tự động fallback sang đọc "roles" để tương thích ngược.
     * Tại sao làm vậy:
     * - Đảm bảo tính linh hoạt và an toàn, không gây NullPointerException nếu token thiếu claim.
     */
    @SuppressWarnings("unchecked")
    public List<String> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<String> authorities = claims.get("authorities", List.class);
        if (authorities == null) {
            authorities = claims.get("roles", List.class);
        }
        return authorities != null ? authorities : Collections.emptyList();
    }

    /**
     * Đoạn này làm gì: Kiểm tra tính hợp lệ của token (chữ ký hợp lệ, chưa hết hạn, cấu trúc chuẩn).
     * Tại sao làm vậy: Bắt các ngoại lệ JwtException để trả về boolean, phục vụ luồng xử lý của Filter.
     */
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
