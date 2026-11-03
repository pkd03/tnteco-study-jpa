package vn.tnteco.study.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.rbac.UserRole;

import java.util.List;

/**
 * Repository quản lý bảng trung gian liên kết User và Role.
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    /**
     * Đoạn này làm gì: Lấy trực tiếp danh sách ID các Role của một User.
     * Tại sao làm vậy: Phục vụ kiểm tra hoặc thu hồi/gán vai trò nhanh mà không cần load entity Role.
     */
    @Query("SELECT ur.roleId FROM UserRole ur WHERE ur.userId = :userId")
    List<String> findRoleIdsByUserId(@Param("userId") String userId);

    // Xóa toàn bộ vai trò của một người dùng (dùng khi gán lại danh sách vai trò mới)
    void deleteByUserId(String userId);

    // Kiểm tra xem người dùng đã được gán vai trò này hay chưa
    boolean existsByUserIdAndRoleId(String userId, String roleId);
}
