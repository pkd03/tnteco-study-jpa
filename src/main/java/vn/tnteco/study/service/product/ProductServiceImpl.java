package vn.tnteco.study.service.product;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tnteco.study.dto.request.product.CreateProductRequest;
import vn.tnteco.study.dto.request.product.UpdateProductRequest;
import vn.tnteco.study.dto.response.product.ProductResponseForAdmin;
import vn.tnteco.study.dto.response.product.ProductResponseForClient;
import vn.tnteco.study.exception.AppException;
import vn.tnteco.study.exception.ErrorCode;
import vn.tnteco.study.model.Product;
import vn.tnteco.study.model.ProductCategory;
import vn.tnteco.study.repository.CategoryRepository;
import vn.tnteco.study.repository.ProductCategoryRepository;
import vn.tnteco.study.repository.ProductRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service triển khai toàn bộ nghiệp vụ quản lý sản phẩm:
 * 1. Phân tầng Product & Category (Client tự động ẩn sp nếu category cha inactive/xóa mềm).
 * 2. Partial Logic: Kiểm tra trùng mã/tên khi thêm mới, sửa (IdNot), khôi phục (restore).
 * 3. Sorting Whitelist: Fallback an toàn về createAt,desc tránh tấn công/lỗi runtime.
 * 4. Quản lý liên kết N-N thông qua bảng trung gian ProductCategory.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductCategoryRepository productCategoryRepository;

    // Danh sách whitelist các trường cho phép sắp xếp theo đặc tả mục 2.D.3
    static Set<String> SORT_WHITELIST = Set.of("name", "price", "createdAt", "createAt", "quantity");

    // =========================================================================
    // 1. NGHIỆP VỤ CLIENT (/products/client/**) - PHÂN TẦNG VỚI CATEGORY
    // =========================================================================

    /**
     * XEM CHI TIẾT SẢN PHẨM CHO CLIENT:
     * Đoạn này làm gì:
     * - Chỉ tìm sản phẩm có active = true, isDelete = false VÀ Category cha đang active = true, isDelete = false.
     * Tại sao làm vậy:
     * - Đảm bảo khách lẻ không thể xem trộm sản phẩm thuộc danh mục đã đóng cửa.
     */
    @Override
    public ProductResponseForClient getProductByIdForClient(String id) {
        Product product = productRepository.findAvailableProductByIdForClient(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponseForClient(product);
    }

    /**
     * XEM DANH SÁCH SẢN PHẨM CHO CLIENT (KÈM SORTING WHITELIST):
     * Đoạn này làm gì:
     * - Áp dụng Sorting Whitelist trước khi query để ngăn lỗi injection hoặc lỗi runtime property.
     * - Gọi câu truy vấn phân tầng Category tại ProductRepository.
     */
    @Override
    public Page<ProductResponseForClient> getAllForClient(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        Page<Product> productPage = productRepository.findAvailableProductsForClient(safePageable);
        return productPage.map(this::toResponseForClient);
    }

    // =========================================================================
    // 2. NGHIỆP VỤ QUẢN TRỊ ADMIN (/products/admin/**)
    // =========================================================================

    @Override
    public Page<ProductResponseForAdmin> getAllForAdmin(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        Page<Product> productPage = productRepository.findByIsDeleteFalse(safePageable);
        return productPage.map(this::toResponseForAdmin);
    }

    @Override
    public Page<ProductResponseForAdmin> getAllSoftDeleteForAdmin(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        Page<Product> productPage = productRepository.findByIsDeleteTrue(safePageable);
        return productPage.map(this::toResponseForAdmin);
    }

    @Override
    public ProductResponseForAdmin getProductByIdForAdmin(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponseForAdmin(product);
    }

    /**
     * THÊM MỚI SẢN PHẨM:
     * Đoạn này làm gì:
     * 1. Validate danh sách categoryIds: Bắt buộc thuộc ít nhất 1 danh mục và danh mục phải tồn tại.
     * 2. Validate trùng code & name: Chỉ chặn nếu trùng ngoài thùng rác (isDelete = false).
     * 3. Lưu Product và lưu các bản ghi ProductCategory tương ứng.
     */
    @Override
    @Transactional
    public ProductResponseForAdmin createProduct(CreateProductRequest request) {
        // 1. Kiểm tra danh mục liên kết (Quy tắc: Sp phải thuộc ít nhất 1 danh mục)
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
        }
        for (String catId : request.getCategoryIds()) {
            if (!categoryRepository.findByIdAndIsDeleteFalse(catId).isPresent()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        // 2. Kiểm tra trùng mã code và tên ngoài thùng rác
        if (productRepository.existsByCodeAndIsDeleteFalse(request.getCode())) {
            throw new AppException(ErrorCode.PRODUCT_CODE_EXISTED);
        }
        if (productRepository.existsByNameAndIsDeleteFalse(request.getName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTED);
        }

        // 3. Khởi tạo và lưu sản phẩm (@PrePersist sẽ tự điền createAt, active=true, isDelete=false)
        Product product = Product.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();
        productRepository.save(product);

        // 4. Lưu liên kết N-N vào bảng ProductCategory
        for (String catId : request.getCategoryIds()) {
            ProductCategory pc = ProductCategory.builder()
                    .productId(product.getId())
                    .categoryId(catId)
                    .build();
            productCategoryRepository.save(pc);
        }

        return toResponseForAdmin(product);
    }

    /**
     * CẬP NHẬT SẢN PHẨM:
     * Đoạn này làm gì:
     * - Chỉ cho phép sửa sản phẩm ngoài thùng rác (isDelete = false).
     * - Dùng existsByCodeAndIdNotAndIsDeleteFalse để cho phép giữ nguyên mã cũ của chính nó,
     *   nhưng chặn nếu đổi sang mã của một sản phẩm khác đang hoạt động.
     * - Cập nhật lại danh mục liên kết nếu có truyền danh sách categoryIds mới.
     */
    @Override
    @Transactional
    public ProductResponseForAdmin updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // 1. Kiểm tra trùng mã code khi sửa (id != currentId)
        if (request.getCode() != null && !request.getCode().equals(product.getCode())) {
            if (productRepository.existsByCodeAndIdNotAndIsDeleteFalse(request.getCode(), id)) {
                throw new AppException(ErrorCode.PRODUCT_CODE_EXISTED);
            }
            product.setCode(request.getCode());
        }

        // 2. Kiểm tra trùng tên khi sửa (id != currentId)
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (productRepository.existsByNameAndIdNotAndIsDeleteFalse(request.getName(), id)) {
                throw new AppException(ErrorCode.PRODUCT_NAME_EXISTED);
            }
            product.setName(request.getName());
        }

        // 3. Cập nhật các trường thông tin cơ bản
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getActive() != null) product.setActive(request.getActive());

        productRepository.save(product);

        // 4. Cập nhật danh mục liên kết (nếu client có truyền danh sách categoryIds)
        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                throw new AppException(ErrorCode.PRODUCT_MUST_HAVE_CATEGORY);
            }
            for (String catId : request.getCategoryIds()) {
                if (!categoryRepository.findByIdAndIsDeleteFalse(catId).isPresent()) {
                    throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
                }
            }
            // Xóa liên kết cũ và gán lại liên kết mới
            productCategoryRepository.deleteByProductId(id);
            for (String catId : request.getCategoryIds()) {
                productCategoryRepository.save(ProductCategory.builder()
                        .productId(id)
                        .categoryId(catId)
                        .build());
            }
        }

        return toResponseForAdmin(product);
    }

    /**
     * XÓA MỀM (Soft Delete -> Đưa vào thùng rác):
     * Đoạn này làm gì:
     * - Đặt isDelete = true, lưu thời điểm deleteAt, gán active = false.
     */
    @Override
    @Transactional
    public void softDeleteProduct(String id) {
        Product product = productRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setDelete(true);
        product.setDeleteAt(LocalDateTime.now());
        product.setActive(false);
        productRepository.save(product);
    }

    /**
     * KHÔI PHỤC TỪ THÙNG RÁC (Restore):
     * Đoạn này làm gì:
     * 1. Tìm sản phẩm trong thùng rác (isDelete = true).
     * 2. Kiểm tra mã hoặc tên có bị một sản phẩm khác ở ngoài lấy mất trong lúc nằm trong thùng rác không.
     * 3. Nếu bị trùng -> CHẶN lập tức và ném mã lỗi PRODUCT_RESTORE_CONFLICT.
     * 4. Nếu không trùng -> Khôi phục: isDelete = false, deleteAt = null.
     */
    @Override
    @Transactional
    public void restoreProduct(String id) {
        Product product = productRepository.findByIdAndIsDeleteTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (productRepository.existsByCodeAndIsDeleteFalse(product.getCode())) {
            throw new AppException(ErrorCode.PRODUCT_RESTORE_CONFLICT);
        }
        if (productRepository.existsByNameAndIsDeleteFalse(product.getName())) {
            throw new AppException(ErrorCode.PRODUCT_RESTORE_CONFLICT);
        }

        product.setDelete(false);
        product.setDeleteAt(null);
        productRepository.save(product);
    }

    /**
     * XÓA HẲN KHỎI HỆ THỐNG (Hard Delete):
     * Đoạn này làm gì: Xóa sạch liên kết ở bảng trung gian ProductCategory trước, sau đó xóa bản ghi Product.
     */
    @Override
    @Transactional
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        productCategoryRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    // =========================================================================
    // 3. TIỆN ÍCH NỘI BỘ (HELPER METHODS)
    // =========================================================================

    /**
     * THUẬT TOÁN SẮP XẾP AN TOÀN (SORTING WHITELIST):
     * Đoạn này làm gì:
     * - Chỉ chấp nhận các trường: ["name", "price", "createdAt", "createAt", "quantity"].
     * - Map "createdAt" sang tên trường entity "createAt".
     * - Nếu client truyền bất kỳ trường nào ngoài danh sách -> Tự động fallback về mặc định "createAt,desc".
     * Tại sao làm vậy:
     * - Triệt tiêu lỗi 500 PropertyReferenceException và ngăn ngừa khai thác rò rỉ cấu trúc CSDL.
     */
    private Pageable applySortingWhitelist(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        }

        List<Sort.Order> safeOrders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            if (SORT_WHITELIST.contains(property)) {
                String entityProp = property.equals("createdAt") ? "createAt" : property;
                safeOrders.add(new Sort.Order(order.getDirection(), entityProp));
            } else {
                // Gặp trường ngoài danh sách whitelist -> Lập tức hủy bỏ và fallback về mặc định
                safeOrders.clear();
                break;
            }
        }

        if (safeOrders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }

    private ProductResponseForClient toResponseForClient(Product product) {
        return ProductResponseForClient.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .build();
    }

    private ProductResponseForAdmin toResponseForAdmin(Product product) {
        List<String> categoryIds = productCategoryRepository.findCategoryIdsByProductId(product.getId());
        return ProductResponseForAdmin.builder()
                .id(product.getId())
                .code(product.getCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .active(product.getActive())
                .isDelete(product.isDelete())
                .categoryIds(categoryIds)
                .build();
    }
}
