package vn.tnteco.study.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Thực thể Category: Danh mục sản phẩm.
 * Lưu ý nghiệp vụ: Nếu active = false, toàn bộ sản phẩm thuộc danh mục này sẽ bị ẩn ở Client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // Mã danh mục duy nhất khi is_delete = false (ví dụ: "DIEN_THOAI", "LAPTOP")
    @Column(name = "code", nullable = false, length = 50)
    String code;

    // Tên danh mục duy nhất khi is_delete = false
    @Column(name = "name", nullable = false, length = 150)
    String name;

    @Column(name = "description", length = 500)
    String description;

    // Trạng thái kinh doanh: true là mở bán, false là tạm dừng/ẩn
    @Column(name = "active", nullable = false)
    Boolean active;

    @Column(name = "create_at")
    LocalDateTime createAt;

    @Column(name = "update_at")
    LocalDateTime updateAt;

    @Column(name = "delete_at")
    LocalDateTime deleteAt;

    @Column(name = "is_delete", nullable = false)
    boolean isDelete;

    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
        if (this.active == null) {
            this.active = true;
        }
        this.isDelete = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
