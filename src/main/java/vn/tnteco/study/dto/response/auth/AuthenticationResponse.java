package vn.tnteco.study.dto.response.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

/**
 * DTO trả về kết quả đăng nhập thành công.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    // Chuỗi JWT Access Token mang theo Claim authorities
    String token;

    // Tên tài khoản người dùng
    String username;

    // Danh sách các quyền hạn chi tiết (ví dụ: ["product:read", "product:create", ...])
    Collection<String> authorities;
}
