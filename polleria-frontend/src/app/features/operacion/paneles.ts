import { Component, computed, effect, inject, signal } from '@angular/core'; import { CurrencyPipe } from '@angular/common'; import { RouterLink, ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http'; import { ApiService } from '../../core/services/api-service'; import { EventosService } from '../../core/services/eventos-service'; import { AuthService } from '../../core/auth/auth-service'; import { mensajeHttp } from '../../shared/http-error'; import { Mesa } from './mapa-mesas'; import { PedidoResponse, etiqueta } from '../pedidos/pedido.model';
interface Ficha { id: number; tipoPedido: string; estado: string; estadoPago: string; ocupacionMesaId: number | null; mesaNumero?: string; detalles: { nombre: string; cantidad: number }[] }
class PanelBase {
  readonly etiqueta = etiqueta;
  protected http = inject(HttpClient); protected api = inject(ApiService); readonly eventos = inject(EventosService); readonly auth = inject(AuthService);
  readonly error = signal(''); readonly ocupado = signal(false);
  protected cambiar(path: string, body: unknown, despues: () => void) { if (this.ocupado()) return; this.ocupado.set(true); this.error.set(''); this.http.patch(this.api.buildUrl(path), body).subscribe({ next: () => { this.ocupado.set(false); despues(); }, error: e => { this.ocupado.set(false); this.error.set(mensajeHttp(e)); } }); }
}
@Component({ selector: 'app-operaciones', template: `<div class="page-heading"><div><p class="eyebrow">Cocina · {{eventos.conectado()?'En línea':'Reconectando'}}</p><h1>Operaciones</h1><p>Gestiona los pedidos recibidos por cocina en tiempo real.</p></div></div><div class="metric-grid"><article><small>Pedidos pendientes</small><strong>{{pedidos().length}}</strong><span>Recibidos para preparar</span></article><article><small>Acción de cocina</small><strong>LISTO</strong><span>Marca el pedido cuando esté preparado</span></article></div><p role="alert">{{error()}}</p><section class="operation-board"><h2>Flujo de cocina · Pendientes</h2><div class="order-grid">@for(p of pedidos();track p.id){<article class="order-ticket"><div class="ticket-top"><strong>#{{p.id}}</strong><span class="type-pill">{{etiqueta(p.tipoPedido)}}</span></div><h3>{{p.mesaNumero?'Mesa '+p.mesaNumero:'Pedido para llevar'}}</h3>@for(d of p.detalles;track $index){<p>{{d.cantidad}} × {{d.nombre}}</p>}@if(auth.tieneRol('OPERACIONES','ADMINISTRADOR')){<button [disabled]="ocupado()" (click)="listo(p.id)">Marcar LISTO</button>}</article>}@empty{<article class="empty-panel">No hay pedidos pendientes de cocina.</article>}</div></section>` })
export class Operaciones extends PanelBase {
  readonly pedidos = signal<Ficha[]>([]); constructor() { super(); effect(() => { this.eventos.revision(); this.cargar(); }); }
  cargar() { this.http.get<Ficha[]>(this.api.buildUrl('/api/v1/operaciones/pedidos')).subscribe({ next: r => this.pedidos.set(r), error: e => this.error.set(mensajeHttp(e)) }); }
  listo(id: number) { this.cambiar('/api/v1/pedidos/' + id + '/estado', { estado: 'LISTO' }, () => this.cargar()); }
}
@Component({ selector: 'app-mesero', template: `<div class="page-heading"><div><p class="eyebrow">Atención del salón</p><h1>Mesero</h1><p>Gestiona mesas y entrega los pedidos que ya están listos.</p></div></div><p role="alert">{{error()}}</p><div class="metric-grid"><article><small>Mesas</small><strong>{{mesas().length}}</strong><span>Plano del establecimiento</span></article><article><small>Pedidos listos</small><strong>{{pedidos().length}}</strong><span>Pendientes de entrega</span></article></div><div class="waiter-layout"><section class="operation-board"><h2>Plano de mesas</h2><div class="staff-tables">@for(m of mesas();track m.id){<button class="staff-table" [class.staff-occupied]="m.estado==='OCUPADA'" [class.staff-reserved]="m.estado==='RESERVADA'" [attr.aria-pressed]="seleccionada()?.id===m.id" (click)="seleccionada.set(m)"><span aria-hidden="true">▦</span><strong>Mesa {{m.numero}}</strong><small>{{m.capacidad}} personas · {{m.estado}}</small></button>}</div></section><section class="operation-board"><h2>Mesa seleccionada</h2>@if(seleccionada();as m){<h3>Mesa {{m.numero}}</h3><p>{{m.capacidad}} personas · {{m.estado}}</p>@if(auth.tieneRol('MESERO','ADMINISTRADOR')){@if(m.estado==='RESERVADA'){<button [disabled]="ocupado()" (click)="mesa(m,false)">Confirmar llegada</button>}@if(m.estado==='OCUPADA'){<button [disabled]="ocupado()" (click)="mesa(m,true)">Liberar mesa</button>}}}@else{<p>Selecciona una mesa para consultar su estado y las acciones disponibles.</p>}</section></div><section class="operation-board"><h2>Pedidos listos para entregar</h2><div class="order-grid">@for(p of fichas();track p.id){<article class="order-ticket"><h3>#{{p.id}} · {{etiqueta(p.tipoPedido)}} {{p.mesaNumero?'· Mesa '+p.mesaNumero:''}}</h3><span class="status-pill">{{p.estadoPago}}</span>@for(d of p.detalles;track $index){<p>{{d.cantidad}} × {{d.nombre}}</p>}@if(auth.tieneRol('MESERO','ADMINISTRADOR')){<button [class.entrega-confirmada]="p.estado==='ENTREGADO'" [disabled]="ocupado()||p.estadoPago!=='PAGADO'||p.estado==='ENTREGADO'" (click)="entregar(p.id)">{{p.estado==='ENTREGADO'?'Entregado':'Marcar ENTREGADO'}}</button>}</article>}@empty{<p>No hay pedidos listos pendientes de entrega.</p>}</div></section>` })
export class Mesero extends PanelBase {
  readonly pedidos = signal<Ficha[]>([]); readonly entregados = signal<Ficha[]>([]); readonly fichas = computed(() => [...this.pedidos().filter(p => !this.entregados().some(e => e.id === p.id)), ...this.entregados()].sort((a, b) => b.id - a.id)); readonly mesas = signal<Mesa[]>([]); readonly seleccionada = signal<Mesa | null>(null); constructor() { super(); effect(() => { this.eventos.revision(); this.cargar(); }); }
  cargar() { this.http.get<Ficha[]>(this.api.buildUrl('/api/v1/mesero/pedidos')).subscribe({ next: r => this.pedidos.set(r), error: e => this.error.set(mensajeHttp(e)) }); this.http.get<Mesa[]>(this.api.buildUrl('/api/v1/mesas')).subscribe({ next: r => { this.mesas.set(r); const id = this.seleccionada()?.id; if (id) this.seleccionada.set(r.find(m => m.id === id) || null); }, error: e => this.error.set(mensajeHttp(e)) }); }
  entregar(id: number) { const pedido = this.pedidos().find(p => p.id === id); if (!pedido || pedido.estadoPago !== 'PAGADO') return; this.cambiar('/api/v1/pedidos/' + id + '/estado', { estado: 'ENTREGADO' }, () => { this.entregados.update(r => [...r.filter(p => p.id !== id), { ...pedido, estado: 'ENTREGADO' }]); this.cargar(); }); }
  mesa(m: Mesa, liberar: boolean) { this.cambiar('/api/v1/mesas/' + m.id + (liberar ? '/liberar' : '/ocupar'), { ocupacionId: m.ocupacionId }, () => this.cargar()); }
}
@Component({
  selector: 'app-caja',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './caja.html',
})
export class Caja extends PanelBase {
  readonly vista = signal<'caja' | 'pendientes' | 'entregados'>('caja');
  readonly titulos = {
    caja: 'Mi caja',
    pendientes: 'Pedidos pendientes',
    entregados: 'Pedidos entregados',
  };
  readonly cargando = signal(true);
  private readonly registros = signal<PedidoResponse[]>([]);
  readonly pedidos = computed(() => {
    const vista = this.vista();
    return this.registros().filter(p => {
      if (p.flujoVersion !== 2) return false;
      if (vista === 'entregados') return p.estado === 'ENTREGADO';
      if (!['MESA', 'PARA_LLEVAR'].includes(p.tipoPedido)) return false;
      if (p.estado === 'ENTREGADO' || p.estado === 'CANCELADO') return false;
      return vista === 'pendientes' || p.estadoPago === 'PENDIENTE';
    });
  });
  readonly totalPendiente = computed(() => this.pedidos().reduce((suma, p) => suma + p.total, 0));

  constructor() {
    super();
    inject(ActivatedRoute).queryParamMap.subscribe(params => {
      const vista = params.get('vista');
      this.vista.set(vista === 'entregados' || vista === 'pendientes' ? vista : 'caja');
    });
    effect(onCleanup => {
      this.eventos.revision();
      const solicitud = this.http.get<PedidoResponse[]>(this.api.buildUrl('/api/v1/pedidos')).subscribe({
        next: pedidos => {
          this.registros.set(pedidos);
          this.cargando.set(false);
          this.error.set('');
        },
        error: err => {
          this.cargando.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
      onCleanup(() => solicitud.unsubscribe());
    });
  }
}
