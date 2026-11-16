package pe.edu.upeu.polleria.catalogo.producto.mapper;

import org.mapstruct.*;
import pe.edu.upeu.polleria.catalogo.categoria.entity.Categoria;
import pe.edu.upeu.polleria.catalogo.categoria.mapper.CategoriaMapper;
import pe.edu.upeu.polleria.catalogo.producto.dto.*;
import pe.edu.upeu.polleria.catalogo.producto.entity.Producto;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductoMapper {
    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "categoria", source = "categoria")
    Producto toEntity(ProductoRequest request, Categoria categoria);

    @Mapping(target = "presentaciones", ignore = true)
    @Mapping(target = "eliminable", ignore = true)
    ProductoResponse toResponse(Producto producto);

    @Mapping(target = "eliminado", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "request.nombre")
    @Mapping(target = "descripcion", source = "request.descripcion")
    @Mapping(target = "categoria", source = "categoria")
    void actualizar(ProductoRequest request, Categoria categoria, @MappingTarget Producto producto);
}
