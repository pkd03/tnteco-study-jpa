package vn.tnteco.study.model.rbac;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Thực thể Permission: Đại diện cho quyền hạn chi tiết (Fine-grained Permission).
 * Chuẩn hóa theo cú pháp <domain>:<action> (ví dụ: "product:read", "product:create").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "permissions")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // Mã quyền hạn duy nhất theo dạng RESTful (domain:action)
    @Column(name = "permission_code", nullable = false, length = 100)
    String permissionCode;

    // Tên hiển thị quyền (ví dụ: "Xem danh sách sản phẩm admin")
    @Column(name = "permission_name", nullable = false, length = 150)
    String permissionName;

    @Column(name = "description", length = 255)
    String description;

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
        this.isDelete = false;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
