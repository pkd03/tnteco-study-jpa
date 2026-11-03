package vn.tnteco.study.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.rbac.User;

import java.util.Optional;

/**
 * Repository quản lý thực thể User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Tìm kiếm User theo username còn hoạt động (phục vụ đăng nhập)
    Optional<User> findByUsernameAndIsDeleteFalse(String username);

    // Tìm kiếm theo ID và chưa bị xóa mềm
    Optional<User> findByIdAndIsDeleteFalse(String id);

    // Kiểm tra trùng username khi tạo mới (chỉ chặn nếu bản ghi trùng ngoài thùng rác)
    boolean existsByUsernameAndIsDeleteFalse(String username);

    // Kiểm tra trùng email khi tạo mới
    boolean existsByEmailAndIsDeleteFalse(String email);

    // Kiểm tra trùng username khi cập nhật thông tin user khác (id != currentId)
    boolean existsByUsernameAndIdNotAndIsDeleteFalse(String username, String id);

    // Kiểm tra trùng email khi cập nhật thông tin user khác
    boolean existsByEmailAndIdNotAndIsDeleteFalse(String email, String id);
}
