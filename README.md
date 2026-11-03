# Product & Category Management System 📦

Hệ thống RESTful API quản lý danh mục và sản phẩm thương mại điện tử chuyên nghiệp được xây dựng trên nền tảng **Spring Boot**, tích hợp cơ chế xác thực phi trạng thái **Spring Security + JWT (Stateless)**, kiến trúc phân quyền chi tiết **Fine-grained RBAC (`domain:action`)**, vòng đời dữ liệu toàn diện với **Thùng rác thông minh (Soft Delete, Restore, Hard Delete)**, kiểm tra xung đột mã/tên theo ngữ cảnh (**Partial Logic**), phân tầng ẩn/hiện dữ liệu tự động giữa Category và Product, cơ chế sắp xếp an toàn (**Sorting Whitelist**), xử lý ngoại lệ tập trung (**Global Exception Handling**) và tài liệu hóa tự động qua **Swagger UI / OpenAPI 3**.

---

## 📑 Mục lục
1. [Công nghệ sử dụng (Tech Stack)](#-công-nghệ-sử-dụng-tech-stack)
2. [Cấu trúc thư mục dự án (Project Structure)](#-cấu-trúc-thư-mục-dự-án-project-structure)
3. [Thiết kế Cơ sở dữ liệu (Database Schema)](#-thiết-kế-cơ-sở-dữ-liệu-database-schema)
4. [Các quy tắc nghiệp vụ cốt lõi (Core Business Rules)](#-các-quy-tắc-nghiệp-vụ-cốt-lõi-core-business-rules)
    - [4.1. Kiến trúc Bảo mật & Fine-grained RBAC](#41-kiến-trúc-bảo-mật--fine-grained-rbac)
    - [4.2. Quan hệ N-N & Nghiệp vụ phân tầng Category - Product](#42-quan-hệ-n-n--nghiệp-vụ-phân-tầng-category---product)
    - [4.3. Vòng đời Thùng rác & Kiểm soát xung đột (Partial Logic)](#43-vòng-đời-thùng-rác--kiểm-soát-xung-đột-partial-logic)
    - [4.4. Cơ chế Sắp xếp an toàn (Sorting Whitelist)](#44-cơ-chế-sắp-xếp-an-toàn-sorting-whitelist)
5. [Danh mục API Endpoints](#-danh-mục-api-endpoints)
    - [5.1. Authentication (Xác thực JWT)](#51-authentication-xác-thực-jwt)
    - [5.2. Client APIs (Dành cho Khách hàng - Public)](#52-client-apis-dành-cho-khách-hàng---public)
    - [5.3. Admin APIs: Quản lý Danh mục (Categories)](#53-admin-apis-quản-lý-danh-mục-categories)
    - [5.4. Admin APIs: Quản lý Sản phẩm (Products)](#54-admin-apis-quản-lý-sản-phẩm-products)
6. [Chuẩn hóa xử lý ngoại lệ & Bảng mã lỗi (Error Codes)](#-chuẩn-hóa-xử-lý-ngoại-lệ--bảng-mã-lỗi-error-codes)
7. [Hướng dẫn cài đặt & Khởi chạy (Getting Started)](#-hướng-dẫn-cài-đặt--khởi-chạy-getting-started)
8. [Tài liệu API tương tác (Swagger UI)](#-tài-liệu-api-tương-tác-swagger-ui)
9. [Hướng dẫn kiểm thử tự động với Postman](#-hướng-dẫn-kiểm-thử-tự-động-với-postman)

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ / Thư viện | Ghi chú / Chi tiết cấu hình |
| :--- | :--- | :--- |
| **Ngôn ngữ** | Java | 17 (LTS) |
| **Framework nền tảng** | Spring Boot | Spring Web MVC, Spring Data JPA, Spring Security |
| **Bảo mật & Token** | Spring Security & JJWT | `io.jsonwebtoken` (0.12.5) - HMAC-SHA256, Stateless JWT |
| **Phân quyền** | Method Security | `@EnableMethodSecurity` + `@PreAuthorize("hasAuthority('...')")` |
| **Mã hóa mật khẩu** | BCrypt | `BCryptPasswordEncoder` (độ dài muối mặc định an toàn) |
| **Cơ sở dữ liệu** | PostgreSQL | 13+ (Khởi tạo DDL tự động qua Hibernate) |
| **ORM / Persistence** | Hibernate / Spring Data JPA | Tối ưu hóa truy vấn phẳng JPQL, chống N+1 Query |
| **Validation** | Jakarta Bean Validation | `spring-boot-starter-validation` (Hibernate Validator) |
| **Quản lý biến môi trường** | `spring-dotenv` | 4.0.0 (Tự động nạp file `.env` khi chạy ứng dụng) |
| **Tài liệu hóa API** | SpringDoc OpenAPI 3 | `springdoc-openapi-starter-webmvc-ui` 3.1.0 (Swagger UI) |
| **Tiện ích & Giảm boilerplate**| Project Lombok | `@Getter`, `@Setter`, `@Builder`, `@FieldDefaults` |
| **Công cụ Build** | Apache Maven | Maven Wrapper (`mvnw`, `mvnw.cmd`) |

---

## 📁 Cấu trúc thư mục dự án (Project Structure)

```text
src/main/java/vn/tnteco/study/
├── StudyApplication.java                      # Lớp khởi chạy ứng dụng Spring Boot
├── config/
│   └── PasswordEncoderConfig.java             # Cấu hình Bean BCryptPasswordEncoder dùng chung
├── controller/
│   ├── AuthController.java                    # API xác thực / đăng nhập (/api/auth/login)
│   ├── CategoryControllerAdmin.java           # API quản trị danh mục (/api/admin/categories)
│   ├── CategoryControllerClient.java          # API công khai xem danh mục (/api/client/categories)
│   ├── ProductControllerAdmin.java            # API quản trị sản phẩm (/api/admin/products)
│   └── ProductControllerClient.java           # API công khai xem sản phẩm (/api/client/products)
├── dto/
│   ├── request/
│   │   ├── category/
│   │   │   ├── CreateCategoryRequest.java     # Validation dữ liệu tạo danh mục
│   │   │   └── UpdateCategoryRequest.java     # Validation dữ liệu cập nhật danh mục
│   │   ├── product/
│   │   │   ├── CreateProductRequest.java      # Validation dữ liệu tạo sản phẩm (kèm categoryIds)
│   │   │   └── UpdateProductRequest.java      # Validation dữ liệu cập nhật sản phẩm
│   │   └── user/
│   │       └── LoginRequest.java              # Validation thông tin đăng nhập (username, password)
│   └── response/
│       ├── auth/
│       │   └── AuthenticationResponse.java    # Kết quả đăng nhập (token, username, authorities)
│       ├── category/
│       │   └── CategoryResponse.java          # DTO thông tin chi tiết danh mục
│       └── product/
│           ├── ProductResponseForAdmin.java   # DTO đầy đủ cho Admin (gồm categoryIds, audit, soft delete)
│           └── ProductResponseForClient.java  # DTO bảo mật cho Khách (chỉ hiển thị name, desc, price, quantity)
├── exception/
│   ├── AppException.java                      # RuntimeException tùy biến chứa ErrorCode
│   ├── ErrorCode.java                         # Enum định nghĩa mã lỗi nghiệp vụ, HTTP status và message
│   ├── ErrorResponse.java                     # Cấu trúc JSON trả về đồng nhất khi xảy ra lỗi
│   └── GlobalExceptionHandler.java            # @RestControllerAdvice bắt và chuẩn hóa mọi lỗi hệ thống
├── model/
│   ├── Category.java                          # Entity bảng `categories` (Soft Delete, Active)
│   ├── Product.java                           # Entity bảng `product` (Soft Delete, Price, Quantity)
│   ├── ProductCategory.java                   # Entity bảng trung gian `product_category` (N-N)
│   └── rbac/
│       ├── Permission.java                    # Entity bảng `permissions` (quyền hạn domain:action)
│       ├── Role.java                          # Entity bảng `roles` (vai trò ADMIN, STAFF)
│       ├── RolePermission.java                # Entity bảng trung gian `role_permissions` (N-N)
│       ├── User.java                          # Entity bảng `users` (tài khoản người dùng)
│       └── UserRole.java                      # Entity bảng trung gian `user_roles` (N-N)
├── repository/
│   ├── CategoryRepository.java                # JPA truy vấn danh mục, thùng rác và partial logic
│   ├── ProductCategoryRepository.java         # Quản lý quan hệ liên kết sản phẩm - danh mục
│   ├── ProductRepository.java                 # Truy vấn phân tầng Category, thùng rác, partial logic
│   └── user/
│       ├── PermissionRepository.java          # Truy vấn phẳng gom toàn bộ permissions của user
│       ├── RolePermissionRepository.java      # Quản lý quan hệ vai trò - quyền hạn
│       ├── RoleRepository.java                # Quản lý vai trò người dùng
│       ├── UserRepository.java                # Tìm kiếm tài khoản người dùng theo username
│       └── UserRoleRepository.java            # Quản lý quan hệ người dùng - vai trò
├── security/
│   ├── JwtAccessDeniedHandler.java            # Bắt lỗi 403 Forbidden -> JSON ErrorResponse chuẩn
│   ├── JwtAuthenticationEntryPoint.java       # Bắt lỗi 401 Unauthorized -> JSON ErrorResponse chuẩn
│   ├── JwtAuthenticationFilter.java           # Filter trích xuất token, nạp quyền vào SecurityContext
│   ├── JwtTokenProvider.java                  # Ký sinh JWT token, giải mã Claim authorities
│   └── SecurityConfig.java                    # Cấu hình SecurityFilterChain & Method Security
└── service/
    ├── auth/
    │   └── AuthenticationService.java         # Xác thực tài khoản, gom quyền và phát hành JWT
    ├── category/
    │   ├── CategoryService.java               # Interface nghiệp vụ danh mục
    │   └── CategoryServiceImpl.java           # Triển khai nghiệp vụ danh mục, thùng rác và sorting whitelist
    └── product/
        ├── ProductService.java                # Interface nghiệp vụ sản phẩm
        └── ProductServiceImpl.java            # Triển khai nghiệp vụ sản phẩm, cascade category và thùng rác
```

---

## 🗄️ Thiết kế Cơ sở dữ liệu (Database Schema)

Hệ thống quản lý dữ liệu trên 8 bảng với kiến trúc quan hệ chặt chẽ, tách rời quyền hạn (Fine-grained RBAC) và quan hệ đa chiều giữa Sản phẩm và Danh mục:

```mermaid
erDiagram
    users ||--o{ user_roles : "gán vai trò"
    roles ||--o{ user_roles : "thuộc về"
    roles ||--o{ role_permissions : "chứa quyền"
    permissions ||--o{ role_permissions : "thuộc về"
    categories ||--o{ product_category : "phân loại"
    product ||--o{ product_category : "gán danh mục"

    users {
        varchar(36) id PK "UUID"
        varchar(50) username UK "Tên đăng nhập"
        varchar(255) password "BCrypt Hash"
        varchar(50) first_name "Họ đệm"
        varchar(50) last_name "Tên"
        varchar(100) email "Thư điện tử"
        date date_of_birth "Ngày sinh"
        boolean is_delete "Cờ xóa mềm"
        timestamp create_at "Thời gian tạo"
        timestamp update_at "Thời gian cập nhật"
        timestamp delete_at "Thời gian xóa"
    }

    roles {
        varchar(36) id PK "UUID"
        varchar(50) role_code "Mã vai trò (ADMIN, STAFF)"
        varchar(100) role_name "Tên hiển thị vai trò"
        varchar(255) description "Mô tả vai trò"
        boolean is_delete "Cờ xóa mềm"
        timestamp create_at "Thời gian tạo"
        timestamp update_at "Thời gian cập nhật"
        timestamp delete_at "Thời gian xóa"
    }

    permissions {
        varchar(36) id PK "UUID"
        varchar(100) permission_code "Mã quyền chuẩn (domain:action)"
        varchar(150) permission_name "Tên hiển thị quyền"
        varchar(255) description "Mô tả quyền hạn"
        boolean is_delete "Cờ xóa mềm"
        timestamp create_at "Thời gian tạo"
        timestamp update_at "Thời gian cập nhật"
        timestamp delete_at "Thời gian xóa"
    }

    user_roles {
        varchar(36) id PK "UUID"
        varchar(36) user_id FK "Liên kết users.id"
        varchar(36) role_id FK "Liên kết roles.id"
        timestamp create_at "Thời gian gán"
    }

    role_permissions {
        varchar(36) id PK "UUID"
        varchar(36) role_id FK "Liên kết roles.id"
        varchar(36) permission_id FK "Liên kết permissions.id"
        timestamp create_at "Thời gian gán"
    }

    categories {
        varchar(36) id PK "UUID"
        varchar(50) code "Mã danh mục (duy nhất ngoài thùng rác)"
        varchar(150) name "Tên danh mục (duy nhất ngoài thùng rác)"
        varchar(500) description "Mô tả danh mục"
        boolean active "Trạng thái kinh doanh"
        boolean is_delete "Cờ xóa mềm"
        timestamp create_at "Thời gian tạo"
        timestamp update_at "Thời gian cập nhật"
        timestamp delete_at "Thời gian xóa"
    }

    product {
        varchar(36) id PK "UUID"
        varchar(20) code "Mã sản phẩm (duy nhất ngoài thùng rác)"
        varchar(150) name "Tên sản phẩm (duy nhất ngoài thùng rác)"
        varchar(1000) description "Mô tả sản phẩm"
        numeric(15,3) price "Đơn giá (> 0)"
        integer quantity "Tồn kho (>= 0)"
        boolean active "Trạng thái mở bán"
        boolean is_delete "Cờ xóa mềm"
        timestamp create_at "Thời gian tạo"
        timestamp update_at "Thời gian cập nhật"
        timestamp delete_at "Thời gian xóa"
    }

    product_category {
        varchar(36) id PK "UUID"
        varchar(36) product_id FK "Liên kết product.id"
        varchar(36) category_id FK "Liên kết categories.id"
        timestamp create_at "Thời gian liên kết"
    }
```

---

## 📌 Các quy tắc nghiệp vụ cốt lõi (Core Business Rules)

### 4.1. Kiến trúc Bảo mật & Fine-grained RBAC
1. **Kiến trúc Stateless**: Toàn bộ phiên làm việc không lưu trạng thái trên Session (`SessionCreationPolicy.STATELESS`). Client xác thực thông qua Header HTTP:
   ```http
   Authorization: Bearer <jwt_token>
   ```
2. **Cơ chế Phân quyền Tinh (Fine-grained RBAC)**:
    - Hệ thống tách rời khái niệm **Role (Vai trò)** và **Permission (Quyền hạn)**:
      $$\text{User} \xrightarrow{\text{N-N}} \text{Role} \xrightarrow{\text{N-N}} \text{Permission}$$
    - Quyền hạn được chuẩn hóa theo định dạng `<domain>:<action>`:
        - Nhóm Sản phẩm: `product:read`, `product:create`, `product:update`, `product:delete`, `product:bin:read`, `product:restore`.
        - Nhóm Danh mục: `category:read`, `category:create`, `category:update`, `category:delete`, `category:bin:read`, `category:restore`.
    - **Tối ưu hóa gom quyền khi Đăng nhập**: `AuthenticationService` gọi đúng 1 câu lệnh JPQL duy nhất kết nối phẳng 5 bảng để lấy toàn bộ `permissionCode` hợp lệ (tự động loại bỏ role/permission đã bị xóa mềm) và nạp vào Claim `authorities` của JWT.
    - **Bảo vệ tầng Method Security**: Bộ lọc `JwtAuthenticationFilter` phân giải trực tiếp `authorities` thành `SimpleGrantedAuthority`. Tại Controller, từng API được bảo vệ độc lập bằng cú pháp `@PreAuthorize("hasAuthority('domain:action')")`.

### 4.2. Quan hệ N-N & Nghiệp vụ phân tầng Category - Product
1. **Ràng buộc liên kết bắt buộc**:
    - Khi tạo mới (`POST /api/admin/products`) hoặc cập nhật (`PUT /api/admin/products/{id}`), sản phẩm bắt buộc phải có thuộc tính `categoryIds` chứa ít nhất một mã danh mục hợp lệ đang hoạt động (`isDelete = false`).
    - Nếu không truyền danh mục $\rightarrow$ Trả về mã lỗi `2006 PRODUCT_MUST_HAVE_CATEGORY` (HTTP 400).
    - Nếu danh mục không tồn tại $\rightarrow$ Trả về mã lỗi `3001 CATEGORY_NOT_FOUND` (HTTP 404).
2. **Quy tắc phân tầng hiển thị Khách hàng (Cascade Visibility)**:
    - Tại API công khai cho khách hàng (`/api/client/products` và `/api/client/products/{id}`), một sản phẩm chỉ được phép hiển thị khi đáp ứng đồng thời:
        1. Sản phẩm có `active = true` và `isDelete = false`.
        2. Danh mục cha liên kết có `active = true` và `isDelete = false`.
        3. Sản phẩm **KHÔNG** thuộc bất kỳ danh mục nào đang bị tắt (`active = false`) hoặc bị xóa mềm (`isDelete = true`).
    - *Hệ quả nghiệp vụ*: Khi Admin tắt `active = false` hoặc xóa mềm một danh mục, toàn bộ sản phẩm thuộc danh mục đó sẽ lập tức bị ẩn khỏi giao diện khách hàng.

### 4.3. Vòng đời Thùng rác & Kiểm soát xung đột (Partial Logic)
1. **Nguyên tắc Duy nhất theo Ngữ cảnh**:
    - Ràng buộc trùng mã (`code`) và tên (`name`) chỉ áp dụng trong phạm vi các bản ghi **đang hoạt động ngoài thùng rác (`isDelete = false`)**.
2. **Thêm mới (Create)**:
    - Cho phép đặt `code` hoặc `name` trùng với bản ghi đã nằm trong thùng rác (`isDelete = true`).
    - Chặn thêm mới nếu trùng với bản ghi đang hoạt động $\rightarrow$ Trả về HTTP `409 Conflict`.
3. **Cập nhật (Update)**:
    - Chặn cập nhật đối với bản ghi đang nằm trong thùng rác $\rightarrow$ Trả về mã lỗi `2004 PRODUCT_IN_TRASH_CANNOT_EDIT` (HTTP 400).
    - Cho phép giữ nguyên mã/tên cũ của chính nó. Nếu đổi sang mã/tên mới, hệ thống kiểm tra đảm bảo không trùng với các bản ghi khác ngoài thùng rác thông qua truy vấn `existsBy...AndIdNotAndIsDeleteFalse`.
4. **Xóa mềm (Soft Delete)**:
    - Đánh dấu `isDelete = true`, ghi nhận thời điểm `deleteAt = LocalDateTime.now()`, và tự động chuyển `active = false`.
5. **Khôi phục (Restore)**:
    - Trước khi đưa bản ghi từ thùng rác trở lại hoạt động, hệ thống kiểm tra xem mã hoặc tên có bị một bản ghi mới ngoài thùng rác chiếm dụng hay không.
    - Nếu có xung đột $\rightarrow$ **Chặn khôi phục**, trả về HTTP `409 Conflict` (`PRODUCT_RESTORE_CONFLICT` hoặc `CATEGORY_RESTORE_CONFLICT`).
    - Nếu hợp lệ $\rightarrow$ Khôi phục trạng thái: `isDelete = false`, `deleteAt = null`.
6. **Xóa vĩnh viễn (Hard Delete)**:
    - Khi gọi API xóa kèm tham số `hard=true`, hệ thống sẽ xóa sạch dữ liệu liên kết ở bảng trung gian (`product_category`) trước khi xóa hoàn toàn bản ghi khỏi CSDL (`DELETE FROM ... WHERE id = ?`).

### 4.4. Cơ chế Sắp xếp an toàn (Sorting Whitelist)
Để ngăn chặn lỗi HTTP 500 do người dùng truyền sai tên trường trong query param `sort` (lỗi `PropertyReferenceException`) và bảo vệ an toàn cấu trúc nội bộ:
- **Product Whitelist**: Chỉ cho phép sắp xếp theo `name`, `price`, `createdAt`, `createAt`, `quantity`.
- **Category Whitelist**: Chỉ cho phép sắp xếp theo `name`, `createdAt`, `createAt`, `code`.
- Tự động chuyển đổi `createdAt` thành trường thực thể `createAt`.
- Nếu phát hiện bất kỳ trường nào nằm ngoài danh sách whitelist $\rightarrow$ Hệ thống tự động fallback về tiêu chí sắp xếp mặc định an toàn: `createAt,desc`.

---

## 📖 Danh mục API Endpoints

### 5.1. Authentication (Xác thực JWT)

#### `POST /api/auth/login`
- **Quyền hạn**: Public (Không yêu cầu Token)
- **Mô tả**: Đăng nhập tài khoản, nhận Access Token JWT chứa danh sách quyền hạn chi tiết (`authorities`).
- **Request Body**:
```json
{
  "username": "admin",
  "password": "adminpassword"
}
```
- **Response thành công (`200 OK`)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6InVzci1hZG1pbi1pZCIsImF1dGhvcml0aWVzIjpbInByb2R1Y3Q6cmVhZ..."]...",
  "username": "admin",
  "authorities": [
    "product:read",
    "product:create",
    "product:update",
    "product:delete",
    "product:bin:read",
    "product:restore",
    "category:read",
    "category:create",
    "category:update",
    "category:delete",
    "category:bin:read",
    "category:restore"
  ]
}
```

---

### 5.2. Client APIs (Dành cho Khách hàng - Public)

> [!NOTE]
> Toàn bộ các API thuộc `/api/client/**` hoàn toàn công khai, không yêu cầu xác thực JWT.

#### `GET /api/client/categories`
- **Mô tả**: Xem danh sách danh mục đang mở bán (`active = true`, `isDelete = false`) kèm phân trang & sắp xếp.
- **Query Params**: `?page=0&size=10&sort=name,asc`
- **Response (`200 OK`)**: Trả về `Page<CategoryResponse>`.

#### `GET /api/client/categories/{id}`
- **Mô tả**: Xem chi tiết 1 danh mục đang hoạt động.
- **Response (`200 OK`)**: Trả về `CategoryResponse`.

#### `GET /api/client/products`
- **Mô tả**: Xem danh sách sản phẩm mở bán có phân tầng Category (chỉ hiện sản phẩm và danh mục cha đang hoạt động).
- **Query Params**: `?page=0&size=10&sort=name,asc`
- **Response (`200 OK`)**: Trả về `Page<ProductResponseForClient>` (chỉ hiển thị `name`, `description`, `price`, `quantity`).

#### `GET /api/client/products/{id}`
- **Mô tả**: Xem chi tiết 1 sản phẩm mở bán theo ID.
- **Response (`200 OK`)**: Trả về `ProductResponseForClient`.

---

### 5.3. Admin APIs: Quản lý Danh mục (Categories)

> [!IMPORTANT]
> Tất cả các endpoint quản trị yêu cầu Header: `Authorization: Bearer <jwt_token>`

| Phương thức | Đường dẫn API | Quyền hạn yêu cầu | Mô tả chức năng |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/admin/categories` | `category:create` | Tạo mới danh mục sản phẩm |
| `GET` | `/api/admin/categories` | `category:read` | Lấy danh sách danh mục quản trị (ngoài thùng rác) |
| `GET` | `/api/admin/categories/{id}` | `category:read` | Xem chi tiết thông tin danh mục theo ID |
| `PUT` | `/api/admin/categories/{id}` | `category:update` | Cập nhật thông tin danh mục |
| `DELETE` | `/api/admin/categories/{id}` | `category:delete` | Xóa mềm danh mục (chuyển vào thùng rác) |
| `DELETE` | `/api/admin/categories/{id}?hard=true` | `category:delete` | Xóa vĩnh viễn danh mục khỏi CSDL |
| `GET` | `/api/admin/categories/bin` | `category:bin:read` | Xem danh sách danh mục trong thùng rác |
| `PATCH` | `/api/admin/categories/bin/{id}/restore` | `category:restore` | Khôi phục danh mục từ thùng rác |

- **Ví dụ Request Body tạo Danh mục (`POST /api/admin/categories`)**:
```json
{
  "code": "CAT_LAPTOP",
  "name": "Máy tính xách tay & Laptop",
  "description": "Laptop học tập, văn phòng và gaming"
}
```

---

### 5.4. Admin APIs: Quản lý Sản phẩm (Products)

| Phương thức | Đường dẫn API | Quyền hạn yêu cầu | Mô tả chức năng |
| :---: | :--- | :--- | :--- |
| `POST` | `/api/admin/products` | `product:create` | Tạo mới sản phẩm (kèm liên kết danh mục) |
| `GET` | `/api/admin/products` | `product:read` | Lấy danh sách sản phẩm quản trị (kèm phân trang an toàn) |
| `GET` | `/api/admin/products/{id}` | `product:read` | Xem chi tiết sản phẩm dành cho Admin |
| `PUT` | `/api/admin/products/{id}` | `product:update` | Cập nhật thông tin sản phẩm và danh mục liên kết |
| `DELETE` | `/api/admin/products/{id}` | `product:delete` | Xóa mềm sản phẩm (chuyển vào thùng rác) |
| `DELETE` | `/api/admin/products/{id}?hard=true` | `product:delete` | Xóa vĩnh viễn sản phẩm khỏi CSDL |
| `GET` | `/api/admin/products/bin` | `product:bin:read` | Xem danh sách sản phẩm trong thùng rác |
| `PATCH` | `/api/admin/products/bin/{id}/restore` | `product:restore` | Khôi phục sản phẩm từ thùng rác |

- **Ví dụ Request Body tạo Sản phẩm (`POST /api/admin/products`)**:
```json
{
  "code": "PROD-001",
  "name": "iPhone 15 Pro Max 256GB Titan Tự Nhiên",
  "description": "Điện thoại Apple chip A17 Pro khung viền titan",
  "price": 29490000.000,
  "quantity": 45,
  "categoryIds": [
    "cat-01"
  ]
}
```

- **Ví dụ Response trả về cho Admin (`ProductResponseForAdmin`)**:
```json
{
  "id": "e8d2e821-4f12-4cf3-a602-77298642939b",
  "code": "PROD-001",
  "name": "iPhone 15 Pro Max 256GB Titan Tự Nhiên",
  "description": "Điện thoại Apple chip A17 Pro khung viền titan",
  "price": 29490000.000,
  "quantity": 45,
  "active": true,
  "isDelete": false,
  "categoryIds": [
    "cat-01"
  ]
}
```

---

## ⚠️ Chuẩn hóa xử lý ngoại lệ & Bảng mã lỗi (Error Codes)

Hệ thống bắt và định dạng tập trung mọi ngoại lệ qua lớp [`GlobalExceptionHandler.java`](file:///d:/intern/w3/code/study/src/main/java/vn/tnteco/study/exception/GlobalExceptionHandler.java).

### Cấu trúc JSON báo lỗi thống nhất (`ErrorResponse`):
```json
{
  "code": 1002,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "errors": {
    "name": "Tên sản phẩm phải từ 2 đến 150 ký tự",
    "categoryIds": "Sản phẩm phải thuộc ít nhất một danh mục"
  },
  "timestamp": "2026-11-04T01:23:45"
}
```

### Bảng tra cứu toàn bộ danh mục mã lỗi (`ErrorCode`):

| Mã lỗi (`code`) | HTTP Status | Định danh Enum | Ý nghĩa & Tình huống kích hoạt |
| :---: | :---: | :--- | :--- |
| **1001** | `400 Bad Request` | `INVALID_KEY` | Mã key định danh không hợp lệ. |
| **1002** | `400 Bad Request` | `INVALID_INPUT` | Vi phạm validation dữ liệu `@Valid` (kèm chi tiết field trong `errors`). |
| **1003** | `404 Not Found` | `PATH_NOT_FOUND` | Gửi request đến đường dẫn không tồn tại trên hệ thống. |
| **1004** | `401 Unauthorized` | `UNAUTHENTICATED` | Chưa xác thực, sai mật khẩu hoặc token không hợp lệ / hết hạn. |
| **1005** | `403 Forbidden` | `UNAUTHORIZED` | Token hợp lệ nhưng không đủ quyền thực hiện thao tác (`@PreAuthorize`). |
| **2001** | `404 Not Found` | `PRODUCT_NOT_FOUND` | Sản phẩm không tồn tại hoặc đã nằm trong thùng rác. |
| **2002** | `409 Conflict` | `PRODUCT_NAME_EXISTED` | Tên sản phẩm đã tồn tại ở một sản phẩm khác ngoài thùng rác. |
| **2003** | `409 Conflict` | `PRODUCT_CODE_EXISTED` | Mã sản phẩm (`code`) đã tồn tại ở sản phẩm khác ngoài thùng rác. |
| **2004** | `400 Bad Request` | `PRODUCT_IN_TRASH_CANNOT_EDIT` | Chặn chỉnh sửa thông tin khi sản phẩm đang nằm trong thùng rác. |
| **2005** | `409 Conflict` | `PRODUCT_RESTORE_CONFLICT` | Chặn khôi phục vì mã/tên đã bị sản phẩm ngoài thùng rác chiếm dụng. |
| **2006** | `400 Bad Request` | `PRODUCT_MUST_HAVE_CATEGORY` | Sản phẩm bắt buộc phải liên kết với ít nhất một danh mục hợp lệ. |
| **3001** | `404 Not Found` | `CATEGORY_NOT_FOUND` | Danh mục không tồn tại hoặc đã nằm trong thùng rác. |
| **3002** | `409 Conflict` | `CATEGORY_NAME_EXISTED` | Tên danh mục đã tồn tại ngoài thùng rác. |
| **3003** | `409 Conflict` | `CATEGORY_CODE_EXISTED` | Mã danh mục (`code`) đã tồn tại ngoài thùng rác. |
| **3004** | `409 Conflict` | `CATEGORY_RESTORE_CONFLICT` | Chặn khôi phục vì mã/tên danh mục đã bị danh mục khác sử dụng. |
| **9999** | `500 Internal Error` | `UNCATEGORIZED_EXCEPTION` | Lỗi ngoại lệ hệ thống chưa phân loại (lỗi kết nối CSDL, NullPointer,...). |

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy (Getting Started)

### 1. Yêu cầu môi trường
- **Java**: JDK 17 trở lên.
- **PostgreSQL**: Phiên bản 13 trở lên, đã tạo database (ví dụ: `study_db`).
- **Git**: Đã cài đặt trên máy.

# 1. Cấu hình Cơ sở dữ liệu PostgreSQL
DB_URL=jdbc:postgresql://localhost:5432/...
DB_USERNAME=...
DB_PASSWORD=...

# 2. Cấu hình JWT Security
# Thời gian sống của Token (mili-giây, ví dụ: 86400000 = 24 giờ)
VALID_DURATION=...

# Khóa bí mật ký Token (Tối thiểu 256 bits / 32 ký tự ngẫu nhiên bảo mật)
SIGNER_KEY=...
```

# 3. Nạp dữ liệu khởi tạo (Data Seed)
Dự án cung cấp sẵn script SQL nạp đầy đủ danh mục, quyền hạn và sản phẩm mẫu tại file:  
👉 [`doc/scriptSQL`](doc/scriptSQL)

**Tài khoản mẫu được tạo sẵn (Mật khẩu mặc định: `123456`)**:
- **Admin**: `username: admin` - Sở hữu toàn quyền quản trị hệ thống (`product:*`, `category:*`).
- **Staff**: `username: staff` - Chỉ sở hữu quyền xem và sửa sản phẩm (`product:read`, `product:update`).


## 📖 Tài liệu API tương tác (Swagger UI)

Dự án tích hợp sẵn SpringDoc OpenAPI UI giúp bạn tra cứu schema và gọi thử trực tiếp trên trình duyệt:

- **Giao diện trực quan Swagger UI**:  
  👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Schema**:  
  👉 [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> [!TIP]
> Nhấn nút **Authorize 🔓** ở góc trên bên phải giao diện Swagger UI và dán `Bearer <token>` để kiểm thử các API quản trị yêu cầu quyền hạn.

---

## 🧪 Hướng dẫn kiểm thử tự động với Postman

Dự án cung cấp trọn bộ kịch bản kiểm thử tự động trong file:  
👉 [`doc/Product_Management_API.postman_collection.json`](doc/Product_Management_API.postman_collection.json)

### Các bước Import & Thực thi:
1. Mở **Postman** $\rightarrow$ Chọn **Import** $\rightarrow$ Chọn file `doc/Product_Management_API.postman_collection.json`.
2. Kiểm tra Collection Variables:
    - `baseUrl`: `http://localhost:8080`
    - `adminUsername`: `admin`
    - `adminPassword`: `123456`
3. Thực thi request đăng nhập tại thư mục **`00. Authentication`** $\rightarrow$ Script tự động lưu JWT token vào biến môi trường `{{adminToken}}` để tự động xác thực cho toàn bộ các request Admin tiếp theo.