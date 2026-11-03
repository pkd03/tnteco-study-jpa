package vn.tnteco.study.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Thực thể bảng trung gian ProductCategory: Mô hình hóa quan hệ N-N giữa Product và Category.
 * Nghiệp vụ quy định: Một sản phẩm phải thuộc ít nhất 1 danh mục.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_category", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "category_id"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // ID sản phẩm liên kết
    @Column(name = "product_id", nullable = false, length = 36)
    String productId;

    // ID danh mục liên kết
    @Column(name = "category_id", nullable = false, length = 36)
    String categoryId;

    // Thời điểm gán sản phẩm vào danh mục
    @Column(name = "create_at")
    LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
    }
}
