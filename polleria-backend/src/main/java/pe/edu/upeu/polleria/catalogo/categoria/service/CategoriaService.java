package pe.edu.upeu.polleria.catalogo.categoria.service;

import java.util.List;
import pe.edu.upeu.polleria.catalogo.categoria.dto.CategoriaRequest;
import pe.edu.upeu.polleria.catalogo.categoria.dto.CategoriaResponse;

public interface CategoriaService {
    List<CategoriaResponse> listar();

    CategoriaResponse obtener(Long id);

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse actualizar(Long id, CategoriaRequest request);

    void eliminar(Long id);
}
