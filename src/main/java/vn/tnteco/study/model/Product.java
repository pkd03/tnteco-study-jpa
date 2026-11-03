package vn.tnteco.study.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "product")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    String id;

    @Column(name = "code", nullable = false, length = 20)
    String code;

    @Column(name = "name", nullable = false, length = 150)
    String name;

    @Column(name = "description", length = 1000)
    String description;

    @Column(name = "price", nullable = false, precision = 15, scale = 3)
    BigDecimal price;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @Column(name = "active")
    Boolean active;

    @Column(name = "create_at")
    LocalDateTime createAt;

    @Column(name = "update_at")
    LocalDateTime updateAt;

    @Column(name = "delete_at")
    LocalDateTime deleteAt;

    @Column(name = "is_delete")
    boolean isDelete;

    //    @PrePersist: Tự động chạy method gán createAt = LocalDateTime.now()
//    và isDelete = false ngay trước khi câu lệnh INSERT SQL được thực thi.
    @PrePersist
    public void prePersist() {
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now();
        }
        this.active = true;
        this.isDelete = false;
    }

    //    @PreUpdate: Tự động chạy method gán updateAt = LocalDateTime.now()
//    ngay trước khi UPDATE SQL được thực thi.
    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }

//    time xóa mềm sẽ được set trong service

}
