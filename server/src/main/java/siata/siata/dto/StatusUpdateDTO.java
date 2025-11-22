package siata.siata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class StatusUpdateDTO {
    
    @NotBlank(message = "Status tidak boleh kosong")
    @Pattern(regexp = "^(Disetujui|Ditolak|Menunggu)$", 
             message = "Status harus salah satu dari: Disetujui, Ditolak, Menunggu")
    private String status;
}
