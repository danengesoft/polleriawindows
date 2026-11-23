package pe.edu.upeu.polleria.clinica.clinica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClinicaMapper {

    @Mapping(target = "id", ignore = true)
    Clinica toEntity(ClinicaRequest request);

    ClinicaResponse toResponse(Clinica clinica);

    @Mapping(target = "id", ignore = true)
    void actualizar(ClinicaRequest request, @MappingTarget Clinica clinica);
}
