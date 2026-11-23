package pe.edu.upeu.polleria.clinica.especialidad.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.mapper.EspecialidadMapper;
import pe.edu.upeu.polleria.clinica.especialidad.repository.EspecialidadRepository;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository repository;
    private final EspecialidadMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar() {
        return repository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
    }
}
