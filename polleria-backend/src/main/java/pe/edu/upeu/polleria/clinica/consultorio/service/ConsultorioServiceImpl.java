package pe.edu.upeu.polleria.clinica.consultorio.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.clinica.repository.ClinicaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;
import pe.edu.upeu.polleria.clinica.consultorio.mapper.ConsultorioMapper;
import pe.edu.upeu.polleria.clinica.consultorio.repository.ConsultorioRepository;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;
import pe.edu.upeu.polleria.clinica.especialidad.repository.EspecialidadRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.InvalidRequestException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ConsultorioServiceImpl implements ConsultorioService {

    private final ConsultorioRepository consultorioRepository;
    private final ClinicaRepository clinicaRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ConsultorioMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listar() {
        return consultorioRepository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultorioResponse obtener(Long id) {
        return mapper.toResponse(buscarOFallar(id));
    }

    /**
     * Registra la cabecera y sus líneas en una sola transacción: si una
     * especialidad no existe, no se guarda nada (rollback).
     */
    @Override
    @Transactional
    public ConsultorioResponse crear(ConsultorioRequest request) {
        String codigo = request.getCodigo().strip().toUpperCase();
        if (consultorioRepository.existsByCodigoIgnoreCase(codigo))
            throw new BusinessConflictException("Ya existe un consultorio con el código " + codigo);

        Clinica clinica = clinicaRepository.findById(request.getClinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clínica no encontrada: " + request.getClinicaId()));

        Set<Long> ids = new LinkedHashSet<>(request.getEspecialidadIds()); // quita repetidos
        List<Especialidad> especialidades = especialidadRepository.findAllById(ids);
        if (especialidades.size() != ids.size())
            throw new InvalidRequestException("Alguna especialidad elegida no existe");

        Consultorio consultorio = new Consultorio();
        consultorio.setClinica(clinica);
        consultorio.setCodigo(codigo);
        consultorio.setTarifaHora(request.getTarifaHora());
        consultorio.setEstado(request.getEstado());
        consultorio.getEspecialidades().addAll(especialidades);

        Consultorio guardado = consultorioRepository.save(consultorio);
        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Consultorio consultorio = buscarOFallar(id);
        // Hibernate borra primero sus filas de CONSULTORIO_ESPECIALIDAD y después la cabecera.
        consultorioRepository.delete(consultorio);
    }

    private Consultorio buscarOFallar(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio no encontrado: " + id));
    }
}
