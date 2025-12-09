package siata.siata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StatusUpdateDTO {
    
    @NotBlank(message = "Status tidak boleh kosong")
    private String status;
    
    @Size(max = 1000, message = "Catatan maksimal 1000 karakter")
    private String catatan; // Optional approver message
    
    @Size(max = 500, message = "Path lampiran maksimal 500 karakter")
    private String lampiran; // Optional attachment path
}
