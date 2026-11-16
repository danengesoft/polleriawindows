package pe.edu.upeu.polleria.clientes.cliente.mapper;

import org.mapstruct.*;
import pe.edu.upeu.polleria.clientes.cliente.dto.*;
import pe.edu.upeu.polleria.clientes.cliente.entity.Cliente;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClienteMapper {
    @Mapping(target = "webSesionId", ignore = true)
    @Mapping(target = "webHuella", ignore = true)
    @Mapping(target = "correoContactoWeb", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakSub", ignore = true)
    Cliente toEntity(ClienteRequest request);

    @Mapping(target = "eliminable", ignore = true)
    ClienteResponse toResponse(Cliente cliente);

    @Mapping(target = "webSesionId", ignore = true)
    @Mapping(target = "webHuella", ignore = true)
    @Mapping(target = "correoContactoWeb", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakSub", ignore = true)
    void actualizar(ClienteRequest request, @MappingTarget Cliente cliente);
}
