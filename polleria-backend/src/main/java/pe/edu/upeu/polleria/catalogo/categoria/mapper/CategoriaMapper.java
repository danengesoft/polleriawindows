package pe.edu.upeu.polleria.catalogo.categoria.mapper;

import org.mapstruct.*;
import pe.edu.upeu.polleria.catalogo.categoria.dto.*;
import pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoriaMapper {
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "id", ignore = true)
    Categoria toEntity(CategoriaRequest request);

    @Mapping(target = "eliminable", ignore = true)
    CategoriaResponse toResponse(Categoria categoria);

    CategoriaResumen toResumen(Categoria categoria);

    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "id", ignore = true)
    void actualizar(CategoriaRequest request, @MappingTarget Categoria categoria);
}
