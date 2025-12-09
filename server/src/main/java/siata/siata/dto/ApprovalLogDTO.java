package siata.siata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLogDTO {
    private String namaPegawai;
    private String role;
    private String status; // "Disetujui" or "Ditolak"
    private LocalDateTime timestamp;
    private String catatan; // Approver's message
    private String lampiran; // File path to attachment
}
