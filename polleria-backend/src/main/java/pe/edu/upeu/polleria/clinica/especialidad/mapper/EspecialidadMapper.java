package pe.edu.upeu.polleria.clinica.especialidad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EspecialidadMapper {
    EspecialidadResponse toResponse(Especialidad especialidad);
}
