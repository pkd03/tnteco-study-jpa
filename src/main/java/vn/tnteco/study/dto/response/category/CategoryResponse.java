package vn.tnteco.study.dto.response.category;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chi tiết danh mục sản phẩm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String code;
    String name;
    String description;
    Boolean active;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    LocalDateTime deleteAt;
    boolean isDelete;
}
