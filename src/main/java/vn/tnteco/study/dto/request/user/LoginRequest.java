package vn.tnteco.study.dto.request.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginRequest {
    @NotBlank(message = "Username là bắt buộc")
    String username;

    @NotBlank(message = "Password là bắt buộc")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    String password;
}
