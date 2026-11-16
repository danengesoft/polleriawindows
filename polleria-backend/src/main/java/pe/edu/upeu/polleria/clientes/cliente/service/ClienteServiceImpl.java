package pe.edu.upeu.polleria.clientes.cliente.service;

import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clientes.cliente.dto.*;
import pe.edu.upeu.polleria.clientes.cliente.entity.Cliente;
import pe.edu.upeu.polleria.clientes.cliente.mapper.ClienteMapper;
import pe.edu.upeu.polleria.clientes.cliente.repository.ClienteRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.InvalidRequestException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl
        implements ClienteService, pe.edu.upeu.polleria.clientes.cliente.api.ClienteConsultaService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream().map(this::respuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id) {
        return respuesta(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        String correo = normalizarCorreo(request.getCorreo());
        if (correo != null && clienteRepository.contarPorCorreo(correo) > 0) {
            throw new BusinessConflictException("Ya existe un cliente con ese correo");
        }
        Cliente cliente = clienteMapper.toEntity(request);
        normalizar(cliente, correo);
        return respuesta(clienteRepository.saveAndFlush(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscarOFallar(id);
        String correo = normalizarCorreo(request.getCorreo());
        if (correo != null && clienteRepository.contarPorCorreoExcluyendoId(correo, id) > 0) {
            throw new BusinessConflictException("Ya existe un cliente con ese correo");
        }
        clienteMapper.actualizar(request, cliente);
        normalizar(cliente, correo);
        return respuesta(clienteRepository.saveAndFlush(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscarOFallar(id);
        if (cliente.getKeycloakSub() != null) throw new BusinessConflictException("Cliente con cuenta registrada; puede desactivarlo");
        try {
            clienteRepository.delete(cliente);
            clienteRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new BusinessConflictException("Cliente con operaciones asociadas; puede desactivarlo");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen consultarCliente(Long id) {
        Cliente cliente = buscarOFallar(id);
        return new pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen(
                cliente.getId(), cliente.getNombres(), cliente.getApellidos());
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen consultarPorSub(String sub) {
        Cliente c = buscarPorSub(sub);
        if (!Boolean.TRUE.equals(c.getActivo()))
            throw new BusinessConflictException("Perfil comercial desactivado");
        return new pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen(c.getId(), c.getNombres(),
                c.getApellidos());
    }

    @Transactional(readOnly = true)
    public ClienteResponse miPerfil(String sub) {
        return respuesta(buscarPorSub(sub));
    }

    @Transactional
    public ClienteResponse crearPerfil(String sub, pe.edu.upeu.polleria.clientes.cliente.dto.PerfilRequest request) {
        if (clienteRepository.findByKeycloakSub(sub).isPresent())
            throw new BusinessConflictException("Ya existe un perfil comercial");
        ClienteResponse creado = crear(request.toInterno());
        Cliente c = buscarOFallar(creado.getId());
        c.setKeycloakSub(sub);
        return respuesta(clienteRepository.saveAndFlush(c));
    }

    @Transactional
    public ClienteResponse actualizarPerfil(String sub,
            pe.edu.upeu.polleria.clientes.cliente.dto.PerfilRequest request) {
        Cliente c = buscarPorSub(sub);
        ClienteRequest interno = request.toInterno();
        interno.setActivo(c.getActivo());
        return actualizar(c.getId(), interno);
    }


    @Override
    @Transactional
    public pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen resolverCompradorWeb(String sesion,String sub,pe.edu.upeu.polleria.clientes.cliente.api.DatosCompradorWeb d,String huella){
        var existente=sub==null?clienteRepository.findByWebSesionIdAndWebHuella(sesion,huella):clienteRepository.findByKeycloakSub(sub);
        Cliente c=existente.orElseGet(()->{
            Cliente nuevo=new Cliente();nuevo.setNombres(d.nombres());nuevo.setApellidos(d.apellidos());nuevo.setTelefono(d.telefono());
            // El correo no verificado es contacto, nunca una prueba de identidad.
            nuevo.setCorreoContactoWeb(d.correo());
            if(sub==null){nuevo.setWebSesionId(sesion);nuevo.setWebHuella(huella);}else nuevo.setKeycloakSub(sub);
            return clienteRepository.saveAndFlush(nuevo);
        });
        if(!Boolean.TRUE.equals(c.getActivo()))throw new BusinessConflictException("Perfil comercial desactivado");
        return new pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen(c.getId(),c.getNombres(),c.getApellidos());
    }

    private Cliente buscarPorSub(String sub) {
        if (sub == null || sub.isBlank() || sub.length() > 255)
            throw new InvalidRequestException("Identidad inválida");
        return clienteRepository.findByKeycloakSub(sub)
                .orElseThrow(() -> new ResourceNotFoundException("Completa tu perfil comercial"));
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.polleria.clientes.cliente.api.ClienteContacto consultarContacto(Long id) {
        Cliente c = buscarOFallar(id);
        return new pe.edu.upeu.polleria.clientes.cliente.api.ClienteContacto(c.getNombres() + " " + c.getApellidos(),
                c.getTelefono());
    }

    private ClienteResponse respuesta(Cliente entidad) {
        ClienteResponse respuesta = clienteMapper.toResponse(entidad);
        respuesta.setEliminable(entidad.getKeycloakSub() == null && clienteRepository.contarReferencias(entidad.getId()) == 0);
        return respuesta;
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + id));
    }

    private String normalizarCorreo(String correo) {
        String valor = opcional(correo);
        return valor == null ? null : valor.toLowerCase(Locale.ROOT);
    }

    private void normalizar(Cliente cliente, String correo) {
        cliente.setNombres(cliente.getNombres().strip().replaceAll("\\s+", " "));
        cliente.setApellidos(cliente.getApellidos().strip().replaceAll("\\s+", " "));
        cliente.setCorreo(correo);
        cliente.setDireccionPrincipal(opcional(cliente.getDireccionPrincipal()));
        cliente.setReferencia(opcional(cliente.getReferencia()));
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
