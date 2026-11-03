package vn.tnteco.study.service.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.tnteco.study.dto.request.category.CreateCategoryRequest;
import vn.tnteco.study.dto.request.category.UpdateCategoryRequest;
import vn.tnteco.study.dto.response.category.CategoryResponse;

/**
 * Interface định nghĩa các nghiệp vụ quản lý danh mục sản phẩm (Category).
 */
public interface CategoryService {

    // Nghiệp vụ dành cho Client
    Page<CategoryResponse> getAllForClient(Pageable pageable);
    CategoryResponse getCategoryByIdForClient(String id);

    // Nghiệp vụ dành cho Admin
    Page<CategoryResponse> getAllForAdmin(Pageable pageable);
    Page<CategoryResponse> getAllSoftDeleteForAdmin(Pageable pageable);
    CategoryResponse getCategoryByIdForAdmin(String id);

    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(String id, UpdateCategoryRequest request);

    void softDeleteCategory(String id);
    void restoreCategory(String id);
    void deleteCategory(String id);
}
