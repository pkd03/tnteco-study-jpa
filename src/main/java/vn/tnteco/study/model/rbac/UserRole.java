package vn.tnteco.study.model.rbac;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // ID người dùng liên kết
    @Column(name = "user_id", nullable = false, length = 36)
    String userId;

    // ID vai trò liên kết
    @Column(name = "role_id", nullable = false, length = 36)
    String roleId;

    // Thời điểm gán vai trò cho người dùng
    @Column(name = "create_at")
    LocalDateTime createAt;

    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
    }
}
