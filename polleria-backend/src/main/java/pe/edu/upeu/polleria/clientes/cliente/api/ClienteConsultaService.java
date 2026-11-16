package pe.edu.upeu.polleria.clientes.cliente.api;

public interface ClienteConsultaService {
    ClienteResumen resolverCompradorWeb(String sesion, String sub, DatosCompradorWeb datos, String huella);

    ClienteResumen consultarCliente(Long id);

    ClienteContacto consultarContacto(Long id);

    ClienteResumen consultarPorSub(String sub);
}
