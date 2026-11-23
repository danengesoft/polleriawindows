package pe.edu.upeu.polleria.clinica.clinica.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;

public interface ClinicaService {
    List<ClinicaResponse> listar();

    ClinicaResponse obtener(Long id);

    ClinicaResponse crear(ClinicaRequest request);

    ClinicaResponse actualizar(Long id, ClinicaRequest request);

    void eliminar(Long id);
}
