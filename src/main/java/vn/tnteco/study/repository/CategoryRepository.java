package vn.tnteco.study.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.Category;

import java.util.Optional;

/**
 * Repository quản lý Danh mục sản phẩm (Category).
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // Chi tiết danh mục ngoài thùng rác
    Optional<Category> findByIdAndIsDeleteFalse(String id);

    // Danh sách danh mục cho Admin (ngoài thùng rác)
    Page<Category> findByIsDeleteFalse(Pageable pageable);

    // Danh sách danh mục cho Client (đang mở bán và ngoài thùng rác)
    Page<Category> findByActiveTrueAndIsDeleteFalse(Pageable pageable);

    // Chi tiết danh mục cho Client (phải đang active và chưa xóa)
    Optional<Category> findByIdAndActiveTrueAndIsDeleteFalse(String id);

    // Kiểm tra trùng mã code khi thêm mới
    boolean existsByCodeAndIsDeleteFalse(String code);

    // Kiểm tra trùng tên danh mục khi thêm mới
    boolean existsByNameAndIsDeleteFalse(String name);

    // Kiểm tra trùng mã code khi cập nhật (id != currentId)
    boolean existsByCodeAndIdNotAndIsDeleteFalse(String code, String id);

    // Kiểm tra trùng tên khi cập nhật (id != currentId)
    boolean existsByNameAndIdNotAndIsDeleteFalse(String name, String id);

    // Danh sách danh mục trong thùng rác (Trash)
    Page<Category> findByIsDeleteTrue(Pageable pageable);

    // Chi tiết danh mục trong thùng rác
    Optional<Category> findByIdAndIsDeleteTrue(String id);
}
