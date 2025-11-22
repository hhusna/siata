package siata.siata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, message = "Password minimal 8 karakter")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", 
             message = "Password harus mengandung minimal 1 huruf besar, 1 huruf kecil, 1 angka, dan 1 simbol (@$!%*?&#)")
    private String password;

    @NotBlank
    @Size(max = 50)
    private String role;

    @NotNull
    private Long nipPegawai;
}

