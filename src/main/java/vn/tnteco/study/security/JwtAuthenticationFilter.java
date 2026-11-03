package vn.tnteco.study.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter chặn mọi HTTP Request đến ứng dụng:
 * 1. Đọc và xác thực tính hợp lệ của JWT Token từ Authorization Header.
 * 2. Đọc trực tiếp Claim 'authorities' chứa các Permission Code.
 * 3. Chuyển đổi trực tiếp thành SimpleGrantedAuthority để set vào SecurityContextHolder.
 * Không truy vấn lại Database tại đây để đảm bảo mô hình Stateless hiệu năng cao.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        // 1. Kiểm tra sự tồn tại và tính hợp lệ của JWT Token
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.extractUsername(token);
            List<String> permissions = jwtTokenProvider.extractAuthorities(token);

            /**
             * CHUYỂN ĐỔI QUYỀN HẠN SANG SIMPLEGRANTEDAUTHORITY:
             * Đoạn này làm gì:
             * - Lấy từng chuỗi quyền (ví dụ: "product:read", "product:create") tạo thành SimpleGrantedAuthority.
             * - TUYỆT ĐỐI KHÔNG thêm tiền tố "ROLE_".
             * Tại sao làm vậy:
             * - Để tương thích chính xác với cú pháp @PreAuthorize("hasAuthority('product:read')") trên Controller.
             * - Tách rời hoàn toàn khỏi mô hình role-based truyền thống.
             */
            List<GrantedAuthority> authorities = (permissions != null)
                    ? permissions.stream()
                    .filter(StringUtils::hasText)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            // 2. Khởi tạo đối tượng Authentication đại diện cho User hiện tại
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 3. Đưa thông tin xác thực vào SecurityContextHolder để Spring Security kiểm soát phân quyền
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Chuyển tiếp request cho filter tiếp theo trong chuỗi FilterChain
        filterChain.doFilter(request, response);
    }

    /**
     * Đoạn này làm gì: Lấy chuỗi Bearer Token từ Header 'Authorization'.
     * Tại sao làm vậy: Chuẩn giao thức OAuth2 / JWT truyền token qua dạng 'Bearer <token>'.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
