package vn.tnteco.study.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.study.dto.response.product.ProductResponseForClient;
import vn.tnteco.study.service.product.ProductService;

/**
 * Controller công khai phục vụ khách hàng xem sản phẩm (Client / Public API).
 * Không yêu cầu đăng nhập, không yêu cầu bất kỳ quyền hạn nào.
 * Dữ liệu tự động được lọc theo điều kiện active và danh mục cha còn hoạt động.
 */
@RestController
@RequestMapping("/api/client/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductControllerClient {

    ProductService productService;

    /**
     * XEM DANH SÁCH SẢN PHẨM CÔNG KHAI:
     * Đoạn này làm gì: Khách vãng lai xem danh sách sản phẩm hợp lệ, có hỗ trợ phân trang và sắp xếp an toàn.
     */
    @GetMapping
    public Page<ProductResponseForClient> getAllForClient(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return productService.getAllForClient(pageable);
    }

    /**
     * XEM CHI TIẾT SẢN PHẨM CÔNG KHAI:
     * Đoạn này làm gì: Xem chi tiết sản phẩm theo ID.
     * Lưu ý: Đã loại bỏ hoàn toàn @PreAuthorize("hasRole('CLIENT')") để ai cũng có thể xem.
     */
    @GetMapping("/{id}")
    public ProductResponseForClient getProductByIdForClient(@PathVariable String id) {
        return productService.getProductByIdForClient(id);
    }
}
