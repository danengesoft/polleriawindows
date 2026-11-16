package pe.edu.upeu.polleria.clientes.cliente.service;

import java.util.List;
import pe.edu.upeu.polleria.clientes.cliente.dto.*;

public interface ClienteService {
    List<ClienteResponse> listar();

    ClienteResponse obtener(Long id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    void eliminar(Long id);

    ClienteResponse miPerfil(String sub);

    ClienteResponse crearPerfil(String sub, PerfilRequest request);

    ClienteResponse actualizarPerfil(String sub, PerfilRequest request);
}
