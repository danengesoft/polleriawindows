export interface VentaResumen {
  cantidadPedidos: number;
  totalMonetario: number;
  ticketPromedio: number;
}
export interface VentaDia {
  fecha: string;
  cantidadVentas: number;
  totalVentas: number;
}
export interface PagoReporte {
  metodoPago: string;
  cantidadVentas: number;
  totalVentas: number;
}
export interface ProductoVendido {
  productoId: number;
  nombreProducto: string;
  cantidadVendida: number;
  importeGenerado: number;
}
export interface ClienteComprador {
  clienteId: number;
  cantidadPedidos: number;
  totalComprado: number;
}
export interface PedidoEstadoReporte {
  estado: string;
  cantidadPedidos: number;
}
export interface PedidoTipoReporte {
  tipoPedido: string;
  cantidadPedidos: number;
}
