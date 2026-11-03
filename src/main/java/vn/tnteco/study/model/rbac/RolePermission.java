package vn.tnteco.study.model.rbac;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Thực thể bảng trung gian RolePermission: Mô hình hóa mối quan hệ N-N giữa Role và Permission.
 * Giúp giải phóng tính phụ thuộc (Role-Permission Decoupling).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "role_permissions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"role_id", "permission_id"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // ID vai trò liên kết
    @Column(name = "role_id", nullable = false, length = 36)
    String roleId;

    // ID quyền hạn liên kết
    @Column(name = "permission_id", nullable = false, length = 36)
    String permissionId;

    // Thời điểm gán quyền cho vai trò
    @Column(name = "create_at")
    LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
    }
}
