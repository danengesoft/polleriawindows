import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { PedidoService } from '../pedidos/pedido-service';
import { ReporteTabla, TablaReporte, Celda } from './reporte-tabla';
import { mensajeHttp } from '../../shared/http-error';
@Component({
  selector: 'app-reportes',
  imports: [FormsModule, ReporteTabla],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css', './reportes-premium.css'],
})
export class Reportes {
  private readonly pedidos = inject(PedidoService);
  private pendientes = new Subscription();
  readonly seccion = signal(-1);
  busqueda = '';
  readonly secciones = [
    { nombre: 'Resumen de ventas', detalle: 'Totales y ticket promedio del periodo.' },
    { nombre: 'Ventas por día', detalle: 'Consulta las ventas en orden cronológico.' },
    { nombre: 'Métodos de pago', detalle: 'Ventas e importes por medio de pago.' },
    { nombre: 'Productos más vendidos', detalle: 'Productos y cantidades vendidos en el periodo.' },
    { nombre: 'Clientes con más compras efectivas', detalle: 'Compras efectivas por cliente.' },
    { nombre: 'Pedidos', detalle: 'Pedidos por estado y modalidad.' },
  ];
  coincide(nombre: string) { return nombre.toLocaleLowerCase().includes(this.busqueda.trim().toLocaleLowerCase()); }
  readonly maxVentas = computed(() => Math.max(1, ...this.dias().filas.map(f => Number(f[2]))));
  barra(valor: string | number, filas: Celda[][], columna: number) { return Number(valor) / Math.max(1, ...filas.map(f => Number(f[columna]))) * 100; }
  fechaDesde = '';
  fechaHasta = '';
  top = 20;
  readonly errorFiltro = signal('');
  readonly rangoAplicado = signal('Todo el historial');
  private tabla() {
    return signal<TablaReporte>({ cargando: true, error: '', filas: [] });
  }
  readonly ventas = this.tabla();
  readonly dias = this.tabla();
  readonly pagos = this.tabla();
  readonly productos = this.tabla();
  readonly clientes = this.tabla();
  readonly estados = this.tabla();
  readonly tipos = this.tabla();
  readonly resumenVista = computed(() => this.ventas().filas[0]);
  readonly puntosVenta = computed(() => { const filas = this.dias().filas; const max = Math.max(1, ...filas.map(f => Number(f[2]))); return filas.map((f, i) => `${20 + (i / Math.max(1, filas.length - 1)) * 460},${170 - Number(f[2]) / max * 145}`).join(' '); });
  readonly totalTipos = computed(() => this.tipos().filas.reduce((s, f) => s + Number(f[1]), 0));
  porcentajeTipo(valor: string | number) { return this.totalTipos() ? Number(valor) / this.totalTipos() * 100 : 0; }
  constructor() {
    inject(DestroyRef).onDestroy(() => this.pendientes.unsubscribe());
    this.aplicar();
  }
  private cargar<T>(
    destino: ReturnType<Reportes['tabla']>,
    fuente: Observable<T>,
    filas: (datos: T) => Celda[][],
  ) {
    destino.set({ cargando: true, error: '', filas: [] });
    this.pendientes.add(
      fuente.subscribe({
        next: (datos) => destino.set({ cargando: false, error: '', filas: filas(datos) }),
        error: (e) => destino.set({ cargando: false, error: mensajeHttp(e), filas: [] }),
      }),
    );
  }
  aplicar() {
    if (this.fechaDesde && this.fechaHasta && this.fechaDesde > this.fechaHasta) {
      this.errorFiltro.set('Desde no puede ser posterior a Hasta.');
      return;
    }
    if (!Number.isInteger(this.top) || this.top < 1 || this.top > 100) {
      this.errorFiltro.set('Top debe estar entre 1 y 100.');
      return;
    }
    this.errorFiltro.set('');
    this.rangoAplicado.set(
      `${this.fechaDesde || 'Inicio del historial'} — ${this.fechaHasta || 'Sin fecha final'} · calendario Perú`,
    );
    this.pendientes.unsubscribe();
    this.pendientes = new Subscription();
    const f = { fechaDesde: this.fechaDesde, fechaHasta: this.fechaHasta };
    this.cargar(this.ventas, this.pedidos.resumen(f), (r) => [
      ['Resumen', r.cantidadPedidos, r.totalMonetario, r.ticketPromedio],
    ]);
    this.cargar(this.dias, this.pedidos.ventasPorDia(f), (r) =>
      [...r].sort((a, b) => a.fecha.localeCompare(b.fecha)).map((x) => [x.fecha, x.cantidadVentas, x.totalVentas]),
    );
    this.cargar(this.pagos, this.pedidos.metodosPago(f), (r) =>
      r.map((x) => [x.metodoPago, x.cantidadVentas, x.totalVentas]),
    );
    this.cargar(this.productos, this.pedidos.productosVendidos({ ...f, top: this.top }), (r) =>
      r.map((x, i) => [
        i + 1,
        x.nombreProducto + ' (#' + x.productoId + ')',
        x.cantidadVendida,
        x.importeGenerado,
      ]),
    );
    this.cargar(this.clientes, this.pedidos.clientesCompradores({ ...f, top: this.top }), (r) =>
      r.map((x) => ['Cliente #' + x.clienteId, x.cantidadPedidos, x.totalComprado]),
    );
    this.cargar(this.estados, this.pedidos.porEstado(f), (r) =>
      [...r.filter(x => ['PENDIENTE', 'LISTO', 'ENTREGADO'].includes(x.estado)).map(x => [x.estado, x.cantidadPedidos] as Celda[]), ...((r.filter(x => !['PENDIENTE', 'LISTO', 'ENTREGADO'].includes(x.estado)).reduce((n, x) => n + x.cantidadPedidos, 0)) ? [['Otros estados históricos', r.filter(x => !['PENDIENTE', 'LISTO', 'ENTREGADO'].includes(x.estado)).reduce((n, x) => n + x.cantidadPedidos, 0)] as Celda[]] : [])],
    );
    this.cargar(this.tipos, this.pedidos.porTipo(f), (r) =>
      [...r.filter(x => ['MESA', 'PARA_LLEVAR'].includes(x.tipoPedido)).map(x => [x.tipoPedido, x.cantidadPedidos] as Celda[]), ...((r.filter(x => !['MESA', 'PARA_LLEVAR'].includes(x.tipoPedido)).reduce((n, x) => n + x.cantidadPedidos, 0)) ? [['Modalidades anteriores (histórico)', r.filter(x => !['MESA', 'PARA_LLEVAR'].includes(x.tipoPedido)).reduce((n, x) => n + x.cantidadPedidos, 0)] as Celda[]] : [])],
    );
  }
}
