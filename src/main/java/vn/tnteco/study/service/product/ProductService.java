package vn.tnteco.study.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import vn.tnteco.study.dto.request.product.CreateProductRequest;
import vn.tnteco.study.dto.request.product.UpdateProductRequest;
import vn.tnteco.study.dto.response.product.ProductResponseForAdmin;
import vn.tnteco.study.dto.response.product.ProductResponseForClient;

public interface ProductService {
//  với khách hàng:
//  Xem chi tiết theo id: đầy đủ thông tin tên sp, giá, chi tiết, tồn kho
    ProductResponseForClient getProductByIdForClient(String id);

//  xem tất cả sp: tên sp, giá, tồn kho
    Page<ProductResponseForClient> getAllForClient(Pageable pageable);

//////////////////////////////////////////////////////////
//   với admin
//xem sp isdelete false(cả active true và false)
Page<ProductResponseForAdmin> getAllForAdmin(Pageable pageable);

// xem thùng rác (sp xóa mềm)
Page<ProductResponseForAdmin> getAllSoftDeleteForAdmin(Pageable pageable);

//    tạo
    ProductResponseForAdmin createProduct(CreateProductRequest createProductRequest);

    //sửa
    ProductResponseForAdmin updateProduct(String id, UpdateProductRequest updateProductRequest);

//    xóa mềm
    void softDeleteProduct(String id);

    //khôi phuc
    void restoreProduct(String id);


//    xóa hẳn sp cả trong/ ngoài thùng rác
    void deleteProduct(String id);

    //xem chi tiết sp
    ProductResponseForAdmin getProductByIdForAdmin(String id);
}
