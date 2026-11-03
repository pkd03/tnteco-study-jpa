package vn.tnteco.study.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.study.dto.request.product.CreateProductRequest;
import vn.tnteco.study.dto.request.product.UpdateProductRequest;
import vn.tnteco.study.dto.response.product.ProductResponseForAdmin;
import vn.tnteco.study.service.product.ProductService;

/**
 * Controller quản trị Sản phẩm dành cho Admin & Staff.
 * Áp dụng phân quyền chi tiết (Fine-grained RBAC):
 * - Tuyệt đối không dùng hasRole('ADMIN').
 * - Từng API được bảo vệ độc lập bằng @PreAuthorize("hasAuthority('product:action')").
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductControllerAdmin {

    ProductService productService;

    /**
     * THÊM MỚI SẢN PHẨM:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:create".
     * Tại sao làm vậy: Chỉ tài khoản sở hữu quyền thêm mới sản phẩm mới được gọi.
     */
    @PreAuthorize("hasAuthority('product:create')")
    @PostMapping
    public ProductResponseForAdmin createProduct(@Valid @RequestBody CreateProductRequest createProductRequest) {
        return productService.createProduct(createProductRequest);
    }

    /**
     * CẬP NHẬT SẢN PHẨM:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:update".
     */
    @PreAuthorize("hasAuthority('product:update')")
    @PutMapping("/{id}")
    public ProductResponseForAdmin updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        return productService.updateProduct(id, updateProductRequest);
    }

    /**
     * XEM DANH SÁCH SẢN PHẨM QUẢN TRỊ:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:read".
     */
    @PreAuthorize("hasAuthority('product:read')")
    @GetMapping
    public Page<ProductResponseForAdmin> getAllForAdmin(
            @PageableDefault(page = 0, size = 10, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return productService.getAllForAdmin(pageable);
    }

    /**
     * XEM CHI TIẾT SẢN PHẨM ADMIN:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:read".
     */
    @PreAuthorize("hasAuthority('product:read')")
    @GetMapping("/{id}")
    public ProductResponseForAdmin getProductByIdForAdmin(@PathVariable String id) {
        return productService.getProductByIdForAdmin(id);
    }

    /**
     * XEM DANH SÁCH THÙNG RÁC (TRASH/BIN):
     * Đoạn này làm gì: Bảo vệ API bằng quyền riêng biệt "product:bin:read".
     * Tại sao làm vậy: Tách biệt quyền xem hàng đang bán với quyền xem thùng rác nội bộ.
     */
    @PreAuthorize("hasAuthority('product:bin:read')")
    @GetMapping("/bin")
    public Page<ProductResponseForAdmin> getAllSoftDeleteForAdmin(
            @PageableDefault(page = 0, size = 10, sort = "deleteAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return productService.getAllSoftDeleteForAdmin(pageable);
    }

    /**
     * XÓA MỀM HOẶC XÓA HẲN SẢN PHẨM:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:delete".
     */
    @PreAuthorize("hasAuthority('product:delete')")
    @DeleteMapping("/{id}")
    public String deleteProduct(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean hard) {
        if (hard) {
            productService.deleteProduct(id);
            return "Đã xóa vĩnh viễn sản phẩm khỏi hệ thống!";
        } else {
            productService.softDeleteProduct(id);
            return "Xóa mềm sản phẩm thành công (đã chuyển vào thùng rác)";
        }
    }

    /**
     * KHÔI PHỤC SẢN PHẨM TỪ THÙNG RÁC:
     * Đoạn này làm gì: Bảo vệ API bằng quyền "product:restore".
     */
    @PreAuthorize("hasAuthority('product:restore')")
    @PatchMapping("/bin/{id}/restore")
    public String restoreProduct(@PathVariable String id) {
        productService.restoreProduct(id);
        return "Khôi phục sản phẩm thành công!";
    }
}
