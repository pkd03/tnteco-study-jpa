package vn.tnteco.study.model.rbac;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    // Khóa chính UUID dạng chuỗi ký tự 36 ký tự
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    String id;

    // Tên tài khoản đăng nhập (duy nhất khi is_delete = false qua partial index)
    @Column(name = "username", nullable = false, length = 50)
    String username;

    // Mật khẩu đã được mã hóa BCrypt
    @Column(name = "password", nullable = false, length = 255)
    String password;

    @Column(name = "first_name", length = 50)
    String firstName;

    @Column(name = "last_name", length = 50)
    String lastName;

    @Column(name = "email", length = 100)
    String email;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    // Thời điểm tạo bản ghi (không cho phép update qua JPA)
    @Column(name = "create_at")
    LocalDateTime createAt;

    // Thời điểm cập nhật thông tin gần nhất
    @Column(name = "update_at")
    LocalDateTime updateAt;

    // Thời điểm xóa mềm (đưa vào thùng rác)
    @Column(name = "delete_at")
    LocalDateTime deleteAt;

    // Cờ đánh dấu xóa mềm: true là trong thùng rác, false là hoạt động bình thường
    @Column(name = "is_delete", nullable = false)
    boolean isDelete;

    /**
     * Tự động khởi tạo thời gian tạo và cờ xóa mềm trước khi INSERT vào DB.
     * Mục đích: Đảm bảo tính nhất quán dữ liệu mà không cần gán thủ công ở Service layer.
     */
    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
        this.isDelete = false;
    }
    /**
     * Tự động cập nhật thời gian sửa đổi trước khi câu lệnh UPDATE được thực thi.
     */
    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
