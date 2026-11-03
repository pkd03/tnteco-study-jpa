package vn.tnteco.study.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // 1. Nhóm lỗi hệ thống chung
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Mã lỗi không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(1002, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    // 2. Nhóm lỗi nghiệp vụ Product
    PRODUCT_NOT_FOUND(2001, "Sản phẩm không tồn tại hoặc trong thùng racs", HttpStatus.NOT_FOUND),
    PRODUCT_NAME_EXISTED(2002, "Tên sản phẩm đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    PRODUCT_CODE_EXISTED(2003, "Mã sản phẩm (code) đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    PRODUCT_IN_TRASH_CANNOT_EDIT(2004, "Sản phẩm đã nằm trong thùng rác, không được phép chỉnh sửa", HttpStatus.BAD_REQUEST),
    PRODUCT_RESTORE_CONFLICT(2005, "Không thể khôi phục vì mã hoặc tên sản phẩm đã được sử dụng bởi sản phẩm khác", HttpStatus.CONFLICT),

    // Nhóm lỗi nghiệp vụ Product bổ sung
    PRODUCT_MUST_HAVE_CATEGORY(2006, "Sản phẩm phải thuộc ít nhất một danh mục hợp lệ", HttpStatus.BAD_REQUEST),

    // Nhóm lỗi nghiệp vụ Category
    CATEGORY_NOT_FOUND(3001, "Danh mục không tồn tại hoặc đã nằm trong thùng rác", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTED(3002, "Tên danh mục đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    CATEGORY_CODE_EXISTED(3003, "Mã danh mục (code) đã tồn tại trong hệ thống", HttpStatus.CONFLICT),
    CATEGORY_RESTORE_CONFLICT(3004, "Không thể khôi phục vì mã hoặc tên danh mục đã được sử dụng bởi danh mục khác", HttpStatus.CONFLICT),

    // 3. Nhóm lỗi xác thực
    PATH_NOT_FOUND(1003, "Đường dẫn không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004, "Chưa xác thực hoặc token không hợp lệ", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005, "Bạn không có quyền truy cập tài nguyên này", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
