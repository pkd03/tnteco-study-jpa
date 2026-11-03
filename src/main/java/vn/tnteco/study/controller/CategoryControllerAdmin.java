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
import vn.tnteco.study.dto.request.category.CreateCategoryRequest;
import vn.tnteco.study.dto.request.category.UpdateCategoryRequest;
import vn.tnteco.study.dto.response.category.CategoryResponse;
import vn.tnteco.study.service.category.CategoryService;

/**
 * Controller quản trị Danh mục sản phẩm (Category Admin).
 * Được bảo vệ chặt chẽ bởi các quyền hạn domain "category:*".
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryControllerAdmin {

    CategoryService categoryService;

    @PreAuthorize("hasAuthority('category:create')")
    @PostMapping
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PreAuthorize("hasAuthority('category:update')")
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return categoryService.updateCategory(id, request);
    }

    @PreAuthorize("hasAuthority('category:read')")
    @GetMapping
    public Page<CategoryResponse> getAllForAdmin(
            @PageableDefault(page = 0, size = 10, sort = "createAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return categoryService.getAllForAdmin(pageable);
    }

    @PreAuthorize("hasAuthority('category:read')")
    @GetMapping("/{id}")
    public CategoryResponse getCategoryByIdForAdmin(@PathVariable String id) {
        return categoryService.getCategoryByIdForAdmin(id);
    }

    @PreAuthorize("hasAuthority('category:bin:read')")
    @GetMapping("/bin")
    public Page<CategoryResponse> getAllSoftDeleteForAdmin(
            @PageableDefault(page = 0, size = 10, sort = "deleteAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return categoryService.getAllSoftDeleteForAdmin(pageable);
    }

    @PreAuthorize("hasAuthority('category:delete')")
    @DeleteMapping("/{id}")
    public String deleteCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean hard) {
        if (hard) {
            categoryService.deleteCategory(id);
            return "Đã xóa vĩnh viễn danh mục khỏi hệ thống!";
        } else {
            categoryService.softDeleteCategory(id);
            return "Xóa mềm danh mục thành công (đã chuyển vào thùng rác)";
        }
    }

    @PreAuthorize("hasAuthority('category:restore')")
    @PatchMapping("/bin/{id}/restore")
    public String restoreCategory(@PathVariable String id) {
        categoryService.restoreCategory(id);
        return "Khôi phục danh mục thành công!";
    }
}
