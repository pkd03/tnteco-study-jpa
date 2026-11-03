package vn.tnteco.study.security;

import jakarta.servlet.DispatcherType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Lớp cấu hình bảo mật trung tâm của hệ thống (Spring Security Configuration).
 * Áp dụng kiến trúc Fine-grained RBAC:
 * 1. Phân quyền thô ở tầng HTTP (Coarse-grained): Tách biệt Public URLs và Authenticated URLs.
 * 2. Phân quyền tinh ở tầng Method (Fine-grained): Ủy thác toàn bộ kiểm tra quyền cho @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt Method Security để sử dụng @PreAuthorize("hasAuthority(...)") trên Controller/Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    JwtAuthenticationFilter jwtAuthenticationFilter;
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * CẤU HÌNH BẢO MẬT CHUỖI FILTER (SecurityFilterChain):
     * Đoạn này làm gì:
     * - Vô hiệu hóa CSRF và CORS mặc định cho mô hình REST API.
     * - Thiết lập cơ chế Session sang STATELESS (không lưu session trên server).
     * - Đăng ký EntryPoint (xử lý 401) và AccessDeniedHandler (xử lý 403).
     * - Định tuyến các URL công khai và yêu cầu xác thực cho các URL quản trị.
     * - Chèn JwtAuthenticationFilter vào trước UsernamePasswordAuthenticationFilter.
     * Tại sao làm theo cách này:
     * - Đảm bảo chuẩn RESTful API không lưu trạng thái (Stateless), phù hợp xác thực qua JWT.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF vì hệ thống sử dụng JWT token trong Authorization Header, không bị tấn công CSRF qua Cookie
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Tắt CORS mặc định (hoặc cấu hình CorsConfigurationSource nếu gọi từ trình duyệt khác domain)
                .cors(AbstractHttpConfigurer::disable)

                // 3. Cấu hình kiến trúc Stateless: Server không duy trì HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Xử lý các ngoại lệ bảo mật tập trung
                .exceptionHandling(exceptions -> exceptions
                        // Khi chưa đăng nhập hoặc token sai/hết hạn -> trả về 401 Unauthorized
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        // Khi đã đăng nhập nhưng không đủ quyền (thiếu permission) -> trả về 403 Forbidden
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                // 5. PHÂN RANH COARSE-GRAINED URLS:
                .authorizeHttpRequests(auth -> auth
                        // A. Nhóm Public Endpoints: Không yêu cầu xác thực
                        .requestMatchers(
                                "/api/auth/**",            // Đăng nhập, làm mới token
                                "/api/client/**",          // API xem danh mục, sản phẩm cho khách
                                "/products/client/**"      // Hỗ trợ alias URL client nếu dùng
                        ).permitAll()

                        // B. Nhóm Swagger / OpenAPI UI: Cho phép lập trình viên truy cập tài liệu API công khai
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // C. Cho phép các request nội bộ xử lý lỗi (tránh bị chặn 403 khi forward exception)
                        .requestMatchers("/error").permitAll()
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR,
                                DispatcherType.FORWARD
                        ).permitAll()

                        // D. Nhóm Protected Endpoints: Mọi API còn lại (bao gồm /api/admin/**) bắt buộc phải có Token hợp lệ
                        // Việc kiểm tra quyền chi tiết (product:read, product:create...) sẽ do @PreAuthorize đảm nhiệm
                        .anyRequest().authenticated()
                )

                // 6. Đăng ký JwtFilter trước filter xác thực mặc định của Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
