package vn.tnteco.study.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.rbac.RolePermission;

import java.util.List;

/**
 * Repository quản lý bảng trung gian liên kết Role và Permission.
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {

    /**
     * Đoạn này làm gì: Lấy danh sách ID các quyền thuộc về một vai trò cụ thể.
     * Tại sao làm vậy: Tránh load toàn bộ đối tượng RolePermission cồng kềnh khi chỉ cần tập hợp ID để phân quyền.
     */
    @Query("SELECT rp.permissionId FROM RolePermission rp WHERE rp.roleId = :roleId")
    List<String> findPermissionIdsByRoleId(@Param("roleId") String roleId);

    // Xóa toàn bộ quyền của một vai trò (dùng khi cập nhật lại quyền hạn cho Role)
    void deleteByRoleId(String roleId);

    // Kiểm tra xem một vai trò đã được gán quyền cụ thể hay chưa (tránh insert trùng)
    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
}
