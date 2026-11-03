package vn.tnteco.study.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.tnteco.study.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    //    Khách
    //    query product active= true and isDelete = false cho khách xem
    Page<Product> findByActiveTrueAndIsDeleteFalse(Pageable pageable);

    Optional<Product> findByIdAndActiveTrueAndIsDeleteFalse(String id);



////////////////////////////////////////////////////////////////////////////////////////////

    //admin
    boolean existsByNameAndIsDeleteFalse( String name);

    boolean existsByCodeAndIsDeleteFalse(String code);


//    Xem danh sách quản lý: isDelete = false (lọc được theo active = true/false).
    Page<Product> findByIsDeleteFalse(Pageable pageable);

//    Xem thùng rác (Trash): isDelete = true
    Page<Product> findByIsDeleteTrue(Pageable pageable);

//    Khôi phục (Restore): Đổi isDelete = false, deleteAt = null.

//    Xóa hẳn (Hard delete): Dùng productRepository.delete(product)

    //các loại findById
    //findByIdAndIsDeleteFalse: sp ngoài thùng rác(xem chi tiết)
    //findByIdAndIsDeleteTrue: sp trong thùng rác( xóa hẳn, khôi phục)
    //findById: trong + ngoài thùng rác

    //xem chi tiết sp
    Optional<Product> findByIdAndIsDeleteFalse(String id);

    Optional<Product> findByIdAndIsDeleteTrue(String id);

    long countByActiveTrue();

//    số sp xóa mềm(trong thùng rác)
    long countByIsDeleteTrue();

    // =========================================================================
    // 1. DÀNH CHO CLIENT (/products/client/**) - NGHIỆP VỤ PHÂN TẦNG CATEGORY
    // =========================================================================
    /**
     * TRUY VẤN DANH SÁCH SẢN PHẨM HỢP LỆ CHO CLIENT:
     * Đoạn này làm gì:
     * - Lấy sản phẩm có p.active = true VÀ p.isDelete = false.
     * - Sản phẩm phải thuộc ít nhất một danh mục đang c.active = true VÀ c.isDelete = false.
     * - Loại trừ hoàn toàn sản phẩm nếu nó thuộc bất kỳ danh mục nào bị ẩn (active = false) hoặc bị xóa mềm (isDelete = true).
     * Tại sao làm theo cách này:
     * - Thực thi triệt để yêu cầu nghiệp vụ: "Nếu Category cha có active = false, thì toàn bộ Product con
     *   thuộc danh mục đó dù có active = true vẫn bị ẩn hoàn toàn ở API /products/client/**".
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN ProductCategory pc ON p.id = pc.productId
        JOIN Category c ON pc.categoryId = c.id
        WHERE p.active = true AND p.isDelete = false
          AND c.active = true AND c.isDelete = false
          AND p.id NOT IN (
              SELECT pc2.productId FROM ProductCategory pc2
              JOIN Category c2 ON pc2.categoryId = c2.id
              WHERE c2.active = false OR c2.isDelete = true
          )
    """)
    Page<Product> findAvailableProductsForClient(Pageable pageable);
    /**
     * TRUY VẤN CHI TIẾT SẢN PHẨM CHO CLIENT:
     * Đoạn này làm gì: Kiểm tra chi tiết 1 sản phẩm xem có thỏa mãn điều kiện hiển thị của Client hay không.
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN ProductCategory pc ON p.id = pc.productId
        JOIN Category c ON pc.categoryId = c.id
        WHERE p.id = :id
          AND p.active = true AND p.isDelete = false
          AND c.active = true AND c.isDelete = false
          AND p.id NOT IN (
              SELECT pc2.productId FROM ProductCategory pc2
              JOIN Category c2 ON pc2.categoryId = c2.id
              WHERE c2.active = false OR c2.isDelete = true
          )
    """)
    Optional<Product> findAvailableProductByIdForClient(@Param("id") String id);
    /**
     * TRUY VẤN SẢN PHẨM THEO DANH MỤC CHO CLIENT:
     * Đoạn này làm gì: Lọc sản phẩm theo một categoryId cụ thể, đảm bảo danh mục đó và sản phẩm đều đang active và chưa xóa.
     */
    @Query("""
        SELECT DISTINCT p FROM Product p
        JOIN ProductCategory pc ON p.id = pc.productId
        JOIN Category c ON pc.categoryId = c.id
        WHERE pc.categoryId = :categoryId
          AND p.active = true AND p.isDelete = false
          AND c.active = true AND c.isDelete = false
          AND p.id NOT IN (
              SELECT pc2.productId FROM ProductCategory pc2
              JOIN Category c2 ON pc2.categoryId = c2.id
              WHERE c2.active = false OR c2.isDelete = true
          )
    """)
    Page<Product> findAvailableProductsByCategoryIdForClient(@Param("categoryId") String categoryId, Pageable pageable);
    // =========================================================================
    // 2. DÀNH CHO ADMIN & KIỂM TRA TRÙNG LẶP (VALIDATION LOGIC)
    // =========================================================================
    // Kiểm tra trùng mã code khi cập nhật (cho phép trùng với thùng rác, chặn nếu trùng với id khác ngoài thùng rác)
    boolean existsByCodeAndIdNotAndIsDeleteFalse(String code, String id);
    // Kiểm tra trùng tên sản phẩm khi cập nhật (id != currentId AND isDelete = false)
    boolean existsByNameAndIdNotAndIsDeleteFalse(String name, String id);

    // Đếm số sản phẩm đang hoạt động
    long countByActiveTrueAndIsDeleteFalse();

}

