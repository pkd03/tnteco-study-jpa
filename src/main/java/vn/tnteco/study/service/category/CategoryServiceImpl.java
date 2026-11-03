package vn.tnteco.study.service.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tnteco.study.dto.request.category.CreateCategoryRequest;
import vn.tnteco.study.dto.request.category.UpdateCategoryRequest;
import vn.tnteco.study.dto.response.category.CategoryResponse;
import vn.tnteco.study.exception.AppException;
import vn.tnteco.study.exception.ErrorCode;
import vn.tnteco.study.model.Category;
import vn.tnteco.study.repository.CategoryRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service triển khai nghiệp vụ Danh mục (Category) với đầy đủ quy tắc Partial Logic và Soft Delete.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    static Set<String> SORT_WHITELIST = Set.of("name", "createdAt", "createAt", "code");

    @Override
    public Page<CategoryResponse> getAllForClient(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        return categoryRepository.findByActiveTrueAndIsDeleteFalse(safePageable)
                .map(this::toResponse);
    }

    @Override
    public CategoryResponse getCategoryByIdForClient(String id) {
        Category category = categoryRepository.findByIdAndActiveTrueAndIsDeleteFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return toResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAllForAdmin(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        return categoryRepository.findByIsDeleteFalse(safePageable)
                .map(this::toResponse);
    }

    @Override
    public Page<CategoryResponse> getAllSoftDeleteForAdmin(Pageable pageable) {
        Pageable safePageable = applySortingWhitelist(pageable);
        return categoryRepository.findByIsDeleteTrue(safePageable)
                .map(this::toResponse);
    }

    @Override
    public CategoryResponse getCategoryByIdForAdmin(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByCodeAndIsDeleteFalse(request.getCode())) {
            throw new AppException(ErrorCode.CATEGORY_CODE_EXISTED);
        }
        if (categoryRepository.existsByNameAndIsDeleteFalse(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        Category category = Category.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        categoryRepository.save(category);

        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (request.getCode() != null && !request.getCode().equals(category.getCode())) {
            if (categoryRepository.existsByCodeAndIdNotAndIsDeleteFalse(request.getCode(), id)) {
                throw new AppException(ErrorCode.CATEGORY_CODE_EXISTED);
            }
            category.setCode(request.getCode());
        }

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByNameAndIdNotAndIsDeleteFalse(request.getName(), id)) {
                throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getActive() != null) category.setActive(request.getActive());

        categoryRepository.save(category);
        return toResponse(category);
    }

    @Override
    @Transactional
    public void softDeleteCategory(String id) {
        Category category = categoryRepository.findByIdAndIsDeleteFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setDelete(true);
        category.setDeleteAt(LocalDateTime.now());
        category.setActive(false); // Khi xóa mềm danh mục, lập tức tắt active để ẩn toàn bộ product con ở Client
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void restoreCategory(String id) {
        Category category = categoryRepository.findByIdAndIsDeleteTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (categoryRepository.existsByCodeAndIsDeleteFalse(category.getCode())) {
            throw new AppException(ErrorCode.CATEGORY_RESTORE_CONFLICT);
        }
        if (categoryRepository.existsByNameAndIsDeleteFalse(category.getName())) {
            throw new AppException(ErrorCode.CATEGORY_RESTORE_CONFLICT);
        }

        category.setDelete(false);
        category.setDeleteAt(null);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryRepository.deleteById(id);
    }

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
                safeOrders.clear();
                break;
            }
        }

        if (safeOrders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .active(category.getActive())
                .createAt(category.getCreateAt())
                .updateAt(category.getUpdateAt())
                .deleteAt(category.getDeleteAt())
                .isDelete(category.isDelete())
                .build();
    }
}
