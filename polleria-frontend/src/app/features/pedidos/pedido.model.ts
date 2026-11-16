export type TipoPedido = 'MESA' | 'PARA_LLEVAR' | 'DELIVERY' | 'PRESENCIAL' | 'RECOJO';
export type MetodoPago = 'EFECTIVO' | 'YAPE' | 'PLIN' | 'TARJETA';
export type EstadoPedido =
  | 'EN_CAMINO'
  | 'PENDIENTE'
  | 'CONFIRMADO'
  | 'EN_PREPARACION'
  | 'LISTO'
  | 'DESPACHADO'
  | 'ENTREGADO'
  | 'CANCELADO';
export type EstadoPago = 'PENDIENTE' | 'PAGADO' | 'ANULADO';
export interface DetallePedidoRequest {
  presentacionId?:number;
  promocionId?:string;
  productoId: number;
  cantidad: number;
}
export interface PedidoRequest {
  clienteId: number;
  mesaId?: number;
  tipoPedido: TipoPedido;
  metodoPago: MetodoPago;
  direccionEntrega: string | null;
  referenciaEntrega: string | null;
  detalles: DetallePedidoRequest[];
}
export interface DetallePedidoResponse {
  presentacionId?:number|null;
  nombrePresentacion?:string|null;
  promocionId?:string|null;
  precioRegular?:number|null;
  productoId: number;
  nombreProducto: string;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
}
export interface PedidoResponse {
  id: number;
  flujoVersion?: number;
  ocupacionMesaId?: number;
  mesaId?:number;
  mesaNumero?:string;
  cliente: { id: number; nombres: string; apellidos: string };
  fecha: string;
  tipoPedido: TipoPedido;
  estado: EstadoPedido;
  metodoPago: MetodoPago;
  estadoPago: EstadoPago;
  direccionEntrega: string | null;
  referenciaEntrega: string | null;
  total: number;
  detalles: DetallePedidoResponse[];
}
export interface PedidoFiltro {
  estado?: EstadoPedido;
  tipoPedido?: TipoPedido;
}
export const TIPOS: TipoPedido[] = ['MESA', 'PARA_LLEVAR'];
export const METODOS: MetodoPago[] = ['EFECTIVO', 'YAPE', 'PLIN', 'TARJETA'];
export const ESTADOS: EstadoPedido[] = ['PENDIENTE', 'LISTO', 'ENTREGADO'];
export function etiqueta(value: string): string {
  if (value === 'DELIVERY') return 'Modalidad anterior (histórico)';
  if (value === 'EN_PREPARACION') return 'En preparación';
  return value.charAt(0) + value.slice(1).toLowerCase().replaceAll('_', ' ');
}
export function transiciones(pedido: PedidoResponse): EstadoPedido[] {
  if(pedido.flujoVersion===1||!TIPOS.includes(pedido.tipoPedido))return [];
  if(pedido.estado==='PENDIENTE')return ['LISTO'];
  if(pedido.estado==='LISTO' && pedido.estadoPago==='PAGADO')return ['ENTREGADO'];
  return [];
}
