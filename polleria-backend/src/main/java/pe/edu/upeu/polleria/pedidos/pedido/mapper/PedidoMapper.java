package pe.edu.upeu.polleria.pedidos.pedido.mapper;

import org.mapstruct.*;
import pe.edu.upeu.polleria.clientes.cliente.api.ClienteResumen;
import pe.edu.upeu.polleria.pedidos.pedido.entity.*;
import pe.edu.upeu.polleria.pedidos.pedido.dto.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PedidoMapper {
    @Mapping(target = "mesaId", ignore = true)
    @Mapping(target = "mesaNumero", ignore = true)
    @Mapping(target = "id", source = "pedido.id")
    @Mapping(target = "cliente", source = "cliente")
    PedidoResponse toResponse(Pedido pedido, ClienteResumen cliente);

    DetallePedidoResponse toDetalleResponse(DetallePedido detalle);
}
