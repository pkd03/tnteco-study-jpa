package vn.tnteco.study.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.rbac.Role;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý thực thể Role (Vai trò).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    // Tìm kiếm Role theo mã vai trò (chưa bị xóa mềm)
    Optional<Role> findByRoleCodeAndIsDeleteFalse(String roleCode);

    // Tìm kiếm Role theo ID và chưa bị xóa mềm
    Optional<Role> findByIdAndIsDeleteFalse(String id);

    // Kiểm tra trùng mã vai trò khi thêm mới
    boolean existsByRoleCodeAndIsDeleteFalse(String roleCode);

    // Kiểm tra trùng mã vai trò khi cập nhật (id != currentId)
    boolean existsByRoleCodeAndIdNotAndIsDeleteFalse(String roleCode, String id);

    // Lấy toàn bộ danh sách Role đang hoạt động
    List<Role> findByIsDeleteFalse();
}
