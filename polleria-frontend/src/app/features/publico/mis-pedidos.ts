import { EstadoBadge } from '../../shared/estado-badge';
import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClientePublicoService } from './cliente-publico-service';
import { PedidoResponse, etiqueta } from '../pedidos/pedido.model';
import { mensajeHttp } from '../../shared/http-error';
@Component({
  selector: 'app-mis-pedidos',
  imports: [EstadoBadge, CurrencyPipe, DatePipe, RouterLink],
  styleUrl: './publico.css',
  template: ` <p class="eyebrow">Tus momentos con Pollería Neyos</p>
    <h1>Mis pedidos</h1>
    <p>Consulta tus pedidos y su estado actual.</p>
    @if (error()) {
      <p role="alert">{{ error() }}</p>
      <a routerLink="/perfil">Revisar mi perfil</a>
    }
    <button (click)="cargar()" [disabled]="cargando()">Actualizar</button>
    @if (cargando()) {
      <p role="status">Consultando pedidos…</p>
    }
    @for (p of pedidos(); track p.id) {
      <article class="linea order-history">
        <h2>
          <a [routerLink]="['/mis-pedidos', p.id]">Pedido #{{ p.id }}</a>
        </h2>
        <p>{{ p.fecha | date: 'dd/MM/yyyy HH:mm' }}</p>
        <p>
          {{ etiqueta(p.tipoPedido) }} <app-estado [valor]="p.estado" /> · Pago
          <app-estado [valor]="p.estadoPago" />
        </p>
        <strong>{{ p.total | currency: 'PEN' : 'S/ ' : '1.2-2' }}</strong>
      </article>
    } @empty {
      @if (!cargando() && !error()) {
        <p>Todavía no tienes pedidos.</p>
      }
    }
    <a routerLink="/catalogo">Ver catálogo</a>`,
})
export class MisPedidos {
  private readonly api = inject(ClientePublicoService);
  readonly pedidos = signal<PedidoResponse[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly etiqueta = etiqueta;
  constructor() {
    this.cargar();
  }
  cargar() {
    this.cargando.set(true);
    this.error.set('');
    this.api.pedidos().subscribe({
      next: (p) => {
        this.pedidos.set(p);
        this.cargando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeHttp(e));
        this.cargando.set(false);
      },
    });
  }
}
