package vn.tnteco.study.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {
    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 20, message = "Mã sản phẩm tối đa 20 ký tự")
    String code;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(min = 2, max = 150, message = "Tên sản phẩm phải từ 2 đến 150 ký tự")
    String name;

    String description;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")
    BigDecimal price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    Integer quantity;

    @NotEmpty(message = "Sản phẩm phải thuộc ít nhất một danh mục")
    List<String> categoryIds;


}
