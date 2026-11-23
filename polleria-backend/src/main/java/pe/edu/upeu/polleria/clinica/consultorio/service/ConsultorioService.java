package pe.edu.upeu.polleria.clinica.consultorio.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;

public interface ConsultorioService {
    List<ConsultorioResponse> listar();

    ConsultorioResponse obtener(Long id);

    ConsultorioResponse crear(ConsultorioRequest request);

    void eliminar(Long id);
}
