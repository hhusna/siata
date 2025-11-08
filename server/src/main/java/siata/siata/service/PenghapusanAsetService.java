package siata.siata.service;

import org.springframework.stereotype.Service;
import siata.siata.entity.PenghapusanAset;
import siata.siata.repository.PenghapusanAsetRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PenghapusanAsetService {
    private final PenghapusanAsetRepository repository;

    public PenghapusanAsetService(PenghapusanAsetRepository repository) {
        this.repository = repository;
    }

    public List<PenghapusanAset> getAll() {
        return repository.findAll();
    }

    public Optional<PenghapusanAset> getById(Long id) {
        return repository.findById(id);
    }

    public PenghapusanAset save(PenghapusanAset data) {
        return repository.save(data);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}