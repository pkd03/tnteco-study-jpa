package vn.tnteco.study.service.auth;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.tnteco.study.dto.request.user.LoginRequest;
import vn.tnteco.study.dto.response.auth.AuthenticationResponse;
import vn.tnteco.study.exception.AppException;
import vn.tnteco.study.exception.ErrorCode;
import vn.tnteco.study.model.rbac.User;
import vn.tnteco.study.repository.user.PermissionRepository;
import vn.tnteco.study.repository.user.UserRepository;
import vn.tnteco.study.security.JwtTokenProvider;

import java.util.Set;

/**
 * Service xử lý nghiệp vụ xác thực (Authentication).
 * Tích hợp Fine-grained RBAC: Truy vấn gom toàn bộ permissions của User khi đăng nhập.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {

    UserRepository userRepository;
    PermissionRepository permissionRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider jwtTokenProvider;

    public AuthenticationResponse login(LoginRequest request) {
        // 1. Tìm User theo username (chỉ chấp nhận tài khoản chưa bị xóa mềm)
        User user = userRepository.findByUsernameAndIsDeleteFalse(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // 2. So khớp mật khẩu đã hash với BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        /**
         * 3. TRUY VẤN GOM TOÀN BỘ QUYỀN HẠN (PERMISSIONS):
         * Đoạn này làm gì:
         * - Gọi câu truy vấn JPQL phẳng tại PermissionRepository để lấy toàn bộ các permissionCode
         *   của User thông qua các Roles mà User đang sở hữu.
         * Tại sao làm theo cách này:
         * - Chỉ mất đúng 1 câu lệnh SQL duy nhất tại thời điểm đăng nhập.
         * - Tự động loại bỏ các Role hoặc Permission đã bị xóa mềm (is_delete = true).
         */
        Set<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());

        /**
         * 4. SINH JWT ACCESS TOKEN:
         * Đoạn này làm gì:
         * - Đóng gói danh sách permissions vào Claim 'authorities' của Token.
         */
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), permissions);

        // 5. Trả về kết quả xác thực kèm danh sách quyền hạn cho Client/Frontend
        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .authorities(permissions)
                .build();
    }
}
