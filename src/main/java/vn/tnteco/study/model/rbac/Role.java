package vn.tnteco.study.model.rbac;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;
    // Mã vai trò chuẩn hóa (ví dụ: "ROLE_ADMIN", "ROLE_STAFF")
    @Column(name = "role_code", nullable = false, length = 50)
    String roleCode;
    // Tên hiển thị mô tả (ví dụ: "Quản trị viên hệ thống", "Nhân viên bán hàng")
    @Column(name = "role_name", nullable = false, length = 100)
    String roleName;
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
