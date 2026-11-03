package vn.tnteco.study.dto.response.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseForAdmin {
    String id;

    String code;

    String name;

    String description;

    BigDecimal price;

    Integer quantity;

    Boolean active;

    boolean isDelete;

    List<String> categoryIds;

}
