import { AuthService } from '../../core/auth/auth-service';
import { EstadoBadge } from '../../shared/estado-badge';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { mensajeHttp } from '../../shared/http-error';
import { PedidoService } from './pedido-service';
import { EstadoPedido, PedidoResponse, etiqueta, transiciones } from './pedido.model';
import { ConfirmarPedido, ConfirmacionPedido } from './confirmar-pedido';
@Component({
  selector: 'app-pedido-detalle',
  imports: [EstadoBadge, CurrencyPipe, DatePipe, RouterLink, MatButtonModule],
  templateUrl: './pedido-detalle.html',
  styleUrl: './pedido.css',
})
export class PedidoDetalle {
  readonly pagoActualizado = () => this.cargar();
  readonly auth = inject(AuthService);
  private readonly service = inject(PedidoService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly pedido = signal<PedidoResponse | null>(null);
  readonly loading = signal(false);
  readonly cambiando = signal(false);
  readonly error = signal('');
  readonly aviso = signal('');
  readonly etiqueta = etiqueta;
  readonly opciones = computed(() => (this.pedido() ? transiciones(this.pedido()!).filter(e => e === 'LISTO' ? this.auth.tieneRol('ADMINISTRADOR', 'OPERACIONES') : this.auth.tieneRol('ADMINISTRADOR', 'MESERO')) : []));
  private readonly id = Number(this.route.snapshot.paramMap.get('id'));
  constructor() {
    const creado: PedidoResponse | undefined = history.state?.['pedidoCreado'];
    if (creado?.id === this.id) {
      this.pedido.set(creado);
      this.aviso.set('Pedido registrado. Importes y estados confirmados por el servidor.');
    }
    // Consulta canónica también al refrescar; el historial del navegador nunca reemplaza al servidor.
    this.cargar();
  }
  cargar() {
    if (!Number.isSafeInteger(this.id) || this.id <= 0) {
      this.error.set('Identificador inválido.');
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.service
      .obtener(this.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.pedido.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.pedido.set(null);
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }
  confirmarPago() {
    const p = this.pedido();
    if (
      !p ||
      !['MESA', 'PARA_LLEVAR'].includes(p.tipoPedido) ||
      this.cambiando() ||
      p.estadoPago !== 'PENDIENTE' ||
      !this.auth.tieneRol('CAJERO', 'ADMINISTRADOR') ||
      p.estado === 'CANCELADO'
    )
      return;
    this.cambiando.set(true);
    this.dialog
      .open(ConfirmarPedido, {
        data: {
          titulo: 'Confirmar pago recibido',
          mensaje:
            'Confirma que verificaste el pago del Pedido #' +
            p.id +
            '. Esta acción no realiza un cobro en línea.',
        },
        width: '30rem',
        maxWidth: '95vw',
      })
      .afterClosed()
      .subscribe((ok) => {
        if (!ok) {
          this.cambiando.set(false);
          return;
        }
        this.service.confirmarPago(p.id).subscribe({
          next: (r) => {
            this.pedido.set(r);
            this.cambiando.set(false);
            this.aviso.set('Pago confirmado.');
          },
          error: (e) => {
            this.error.set(mensajeHttp(e));
            this.cambiando.set(false);
          },
        });
      });
  }
  cambiarEstado(estado: EstadoPedido) {
    const pedido = this.pedido();
    if (!pedido || this.cambiando() || this.loading() || !this.opciones().includes(estado)) return;
    const data: ConfirmacionPedido = {
      titulo: estado === 'CANCELADO' ? 'Cancelar pedido' : 'Cambiar estado',
      mensaje: `Pedido #${pedido.id}: ${etiqueta(pedido.estado)} → ${etiqueta(estado)}. ${estado === 'CANCELADO' ? 'El pedido conservará su historial.' : ''}`,
    };
    this.cambiando.set(true);
    this.dialog
      .open(ConfirmarPedido, { data, width: '30rem', maxWidth: 'calc(100vw - 2rem)' })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado !== true) {
          this.cambiando.set(false);
          return;
        }
        this.error.set('');
        this.aviso.set('');
        this.service
          .cambiarEstado(pedido.id, estado)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (data) => {
              this.pedido.set(data);
              this.cambiando.set(false);
              this.aviso.set('Estado actualizado por el servidor.');
            },
            error: (err) => {
              this.cambiando.set(false);
              this.error.set(mensajeHttp(err));
            },
          });
      });
  }
}
