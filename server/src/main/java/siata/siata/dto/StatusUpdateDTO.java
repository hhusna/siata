package siata.siata.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateDTO {
    
    @NotBlank(message = "Status tidak boleh kosong")
    private String status;
}
