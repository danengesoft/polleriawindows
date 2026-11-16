import {
  VentaResumen,
  VentaDia,
  PagoReporte,
  ProductoVendido,
  ClienteComprador,
  PedidoEstadoReporte,
  PedidoTipoReporte,
} from './reporte.model';
import { ReporteFiltro, parametrosReporte } from '../../shared/reporte-filtro';
import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import { EstadoPedido, PedidoFiltro, PedidoRequest, PedidoResponse } from './pedido.model';
@Injectable({ providedIn: 'root' })
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly resource = '/api/v1/pedidos';
  listar(filtro: PedidoFiltro = {}) {
    let params = new HttpParams();
    if (filtro.estado) params = params.set('estado', filtro.estado);
    if (filtro.tipoPedido) params = params.set('tipoPedido', filtro.tipoPedido);
    return this.http.get<PedidoResponse[]>(this.api.buildUrl(this.resource), { params });
  }
  obtener(id: number) {
    return this.http.get<PedidoResponse>(this.api.buildUrl(`${this.resource}/${id}`));
  }
  crear(request: PedidoRequest) {
    return this.http.post<PedidoResponse>(this.api.buildUrl(this.resource), request);
  }
  confirmarPago(id: number) {
    return this.http.patch<PedidoResponse>(
      this.api.buildUrl(`${this.resource}/${id}/confirmar-pago`),
      {},
    );
  }
  cambiarEstado(id: number, estado: EstadoPedido) {
    return this.http.patch<PedidoResponse>(this.api.buildUrl(`${this.resource}/${id}/estado`), {
      estado,
    });
  }
  resumen(f: ReporteFiltro = {}) {
    return this.http.get<VentaResumen>(this.api.buildUrl('/api/v1/pedidos/reportes/resumen'), {
      params: parametrosReporte(f),
    });
  }
  ventasPorDia(f: ReporteFiltro = {}) {
    return this.http.get<VentaDia[]>(this.api.buildUrl('/api/v1/pedidos/reportes/ventas-por-dia'), {
      params: parametrosReporte(f),
    });
  }
  metodosPago(f: ReporteFiltro = {}) {
    return this.http.get<PagoReporte[]>(
      this.api.buildUrl('/api/v1/pedidos/reportes/metodos-pago'),
      { params: parametrosReporte(f) },
    );
  }
  productosVendidos(f: ReporteFiltro = {}) {
    return this.http.get<ProductoVendido[]>(
      this.api.buildUrl('/api/v1/pedidos/reportes/productos-mas-vendidos'),
      { params: parametrosReporte(f) },
    );
  }
  clientesCompradores(f: ReporteFiltro = {}) {
    return this.http.get<ClienteComprador[]>(
      this.api.buildUrl('/api/v1/pedidos/reportes/clientes'),
      { params: parametrosReporte(f) },
    );
  }
  porEstado(f: ReporteFiltro = {}) {
    return this.http.get<PedidoEstadoReporte[]>(
      this.api.buildUrl('/api/v1/pedidos/reportes/por-estado'),
      { params: parametrosReporte(f) },
    );
  }
  porTipo(f: ReporteFiltro = {}) {
    return this.http.get<PedidoTipoReporte[]>(
      this.api.buildUrl('/api/v1/pedidos/reportes/por-tipo'),
      { params: parametrosReporte(f) },
    );
  }
}
