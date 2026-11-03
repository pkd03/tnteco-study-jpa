package vn.tnteco.study.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.study.dto.response.category.CategoryResponse;
import vn.tnteco.study.service.category.CategoryService;

/**
 * Controller công khai phục vụ khách hàng xem danh mục mở bán.
 */
@RestController
@RequestMapping("/api/client/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryControllerClient {

    CategoryService categoryService;

    @GetMapping
    public Page<CategoryResponse> getAllForClient(
            @PageableDefault(page = 0, size = 10, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return categoryService.getAllForClient(pageable);
    }

    @GetMapping("/{id}")
    public CategoryResponse getCategoryByIdForClient(@PathVariable String id) {
        return categoryService.getCategoryByIdForClient(id);
    }
}
