package pe.edu.upeu.polleria.clinica.clinica.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.clinica.mapper.ClinicaMapper;
import pe.edu.upeu.polleria.clinica.clinica.repository.ClinicaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.repository.ConsultorioRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ClinicaServiceImpl implements ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final ConsultorioRepository consultorioRepository;
    private final ClinicaMapper clinicaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClinicaResponse> listar() {
        return clinicaRepository.findAll(Sort.by("razonSocial")).stream().map(clinicaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicaResponse obtener(Long id) {
        return clinicaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ClinicaResponse crear(ClinicaRequest request) {
        normalizar(request);
        if (clinicaRepository.existsByRuc(request.getRuc()))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        Clinica clinica = clinicaMapper.toEntity(request);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public ClinicaResponse actualizar(Long id, ClinicaRequest request) {
        Clinica clinica = buscarOFallar(id);
        normalizar(request);
        if (clinicaRepository.existsByRucAndIdNot(request.getRuc(), id))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        clinicaMapper.actualizar(request, clinica);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Clinica clinica = buscarOFallar(id);
        // La clave foránea CONSULTORIOS.ID_CLINICA impediría borrarla: se avisa con un mensaje claro.
        if (consultorioRepository.existsByClinicaId(id))
            throw new BusinessConflictException("No se puede eliminar: la clínica tiene consultorios registrados");
        clinicaRepository.delete(clinica);
    }

    private Clinica buscarOFallar(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica no encontrada: " + id));
    }

    private void normalizar(ClinicaRequest request) {
        request.setRuc(request.getRuc().strip());
        request.setRazonSocial(request.getRazonSocial().strip());
        if (request.getTelefono() != null) {
            String telefono = request.getTelefono().strip();
            request.setTelefono(telefono.isEmpty() ? null : telefono);
        }
    }
}
