package vn.tnteco.study.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.rbac.Permission;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository quản lý thực thể Permission và thực hiện truy vấn gom quyền cốt lõi.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    // Tìm quyền theo mã và chưa bị xóa mềm
    Optional<Permission> findByPermissionCodeAndIsDeleteFalse(String permissionCode);

    // Kiểm tra trùng mã quyền khi tạo mới
    boolean existsByPermissionCodeAndIsDeleteFalse(String permissionCode);

    // Lấy toàn bộ quyền đang hoạt động trong hệ thống
    List<Permission> findByIsDeleteFalse();

    /**
     * TRUY VẤN GOM QUYỀN THEO USERNAME (Core Security Query):
     * Đoạn này làm gì:
     * - Kết nối 5 bảng: User -> UserRole -> Role -> RolePermission -> Permission.
     * - Chỉ lấy các quyền của User còn hoạt động, Role chưa bị xóa mềm, và Permission chưa bị xóa mềm.
     * Tại sao làm theo cách này:
     * - Trả về Set<String> các permission_code chuẩn hóa (ví dụ: "product:read", "product:create").
     * - Chỉ tốn DUY NHẤT 1 round-trip truy vấn tới Database lúc đăng nhập, triệt tiêu hoàn toàn lỗi N+1 Query.
     * - Phục vụ nạp thẳng vào Claim 'authorities' của JWT token ở Bước 3.
     */
    @Query("""
        SELECT DISTINCT p.permissionCode
        FROM vn.tnteco.study.model.rbac.User u
        JOIN vn.tnteco.study.model.rbac.UserRole ur ON u.id = ur.userId
        JOIN vn.tnteco.study.model.rbac.Role r ON ur.roleId = r.id
        JOIN vn.tnteco.study.model.rbac.RolePermission rp ON r.id = rp.roleId
        JOIN vn.tnteco.study.model.rbac.Permission p ON rp.permissionId = p.id
        WHERE u.username = :username
          AND u.isDelete = false
          AND r.isDelete = false
          AND p.isDelete = false
    """)
    Set<String> findPermissionCodesByUsername(@Param("username") String username);

    /**
     * TRUY VẤN GOM QUYỀN THEO USER ID:
     * Đoạn này làm gì:
     * - Tương tự như trên nhưng lọc trực tiếp theo userId của bảng UserRole.
     * Tại sao làm theo cách này:
     * - Sử dụng khi hệ thống đã có sẵn userId từ SecurityContext hoặc Refresh Token để cập nhật quyền nhanh chóng.
     */
    @Query("""
        SELECT DISTINCT p.permissionCode
        FROM vn.tnteco.study.model.rbac.UserRole ur
        JOIN vn.tnteco.study.model.rbac.Role r ON ur.roleId = r.id
        JOIN vn.tnteco.study.model.rbac.RolePermission rp ON r.id = rp.roleId
        JOIN vn.tnteco.study.model.rbac.Permission p ON rp.permissionId = p.id
        WHERE ur.userId = :userId
          AND r.isDelete = false
          AND p.isDelete = false
    """)
    Set<String> findPermissionCodesByUserId(@Param("userId") String userId);
}
