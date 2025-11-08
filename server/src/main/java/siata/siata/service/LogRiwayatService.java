package siata.siata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import siata.siata.entity.LogRiwayat;
import siata.siata.repository.LogRiwayatRepository;
import java.util.List;

@Service
public class LogRiwayatService {

    @Autowired
    private LogRiwayatRepository logRiwayatRepository;

    public void saveLog(LogRiwayat log) {
        logRiwayatRepository.save(log);
    }

    public List<LogRiwayat> getAllLogs() {
        return logRiwayatRepository.findAllByOrderByTimestampDesc();
    }
}