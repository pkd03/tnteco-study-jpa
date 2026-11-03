package vn.tnteco.study.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.ProductCategory;

import java.util.List;

/**
 * Repository quản lý bảng trung gian liên kết Sản phẩm và Danh mục.
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, String> {

    /**
     * Đoạn này làm gì: Lấy danh sách categoryId mà một sản phẩm đang thuộc về.
     * Tại sao làm vậy: Kiểm tra sản phẩm thuộc danh mục nào một cách nhanh chóng.
     */
    @Query("SELECT pc.categoryId FROM ProductCategory pc WHERE pc.productId = :productId")
    List<String> findCategoryIdsByProductId(@Param("productId") String productId);

    // Xóa toàn bộ liên kết danh mục của một sản phẩm (dùng khi cập nhật lại danh mục)
    void deleteByProductId(String productId);

    /**
     * Đoạn này làm gì: Kiểm tra sản phẩm có thuộc ít nhất một danh mục nào hay không.
     * Tại sao làm vậy: Đáp ứng quy tắc nghiệp vụ: "Một sản phẩm phải thuộc ít nhất 1 danh mục".
     */
    boolean existsByProductId(String productId);

    // Kiểm tra liên kết cụ thể giữa sản phẩm và danh mục đã tồn tại chưa
    boolean existsByProductIdAndCategoryId(String productId, String categoryId);
}
