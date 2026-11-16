import { EstadoBadge } from '../../shared/estado-badge';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Subscription } from 'rxjs';
import { mensajeHttp } from '../../shared/http-error';
import { PedidoService } from './pedido-service';
import { ESTADOS, TIPOS, EstadoPedido, TipoPedido, PedidoResponse, etiqueta } from './pedido.model';
@Component({
  selector: 'app-pedido-list',
  imports: [
    EstadoBadge,
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
  ],
  templateUrl: './pedido-list.html',
})
export class PedidoList {
  private readonly service = inject(PedidoService);
  private readonly destroyRef = inject(DestroyRef);
  private consulta?: Subscription;
  readonly seleccionado=signal<PedidoResponse|null>(null);
  readonly pedidos = signal<PedidoResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly estado = signal<EstadoPedido | null>(null);
  readonly tipo = signal<TipoPedido | null>(null);
  readonly estados = ESTADOS;
  readonly tipos = TIPOS;
  readonly etiqueta = etiqueta;
  constructor() {
    this.cargar();
  }
  filtrarEstado(value: EstadoPedido | null) {
    this.estado.set(value);
    this.cargar();
  }
  filtrarTipo(value: TipoPedido | null) {
    this.tipo.set(value);
    this.cargar();
  }
  cargar() {
    this.consulta?.unsubscribe();
    this.loading.set(true);
    this.error.set('');
    this.consulta = this.service
      .listar({ estado: this.estado() ?? undefined, tipoPedido: this.tipo() ?? undefined })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.pedidos.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }
}
