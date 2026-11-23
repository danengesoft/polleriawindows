package pe.edu.upeu.polleria.clinica.consultorio.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;
import pe.edu.upeu.polleria.clinica.especialidad.mapper.EspecialidadMapper;

@Mapper(componentModel = "spring", uses = EspecialidadMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ConsultorioMapper {

    @Mapping(target = "clinicaId", source = "clinica.id")
    @Mapping(target = "clinicaRazonSocial", source = "clinica.razonSocial")
    ConsultorioResponse toResponse(Consultorio consultorio);
}
