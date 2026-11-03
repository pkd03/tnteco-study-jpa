package vn.tnteco.study.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO yêu cầu tạo mới danh mục sản phẩm.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {

    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 50, message = "Mã danh mục tối đa 50 ký tự")
    String code;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 150, message = "Tên danh mục phải từ 2 đến 150 ký tự")
    String name;

    String description;
}
