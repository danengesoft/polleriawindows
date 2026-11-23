import { WebCompraService, PedidoSeguimiento } from './web-compra-service';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { IconoCliente } from './icono-cliente';
import { SeguimientoPedido } from '../../shared/seguimiento-pedido';
import { EstadoBadge } from '../../shared/estado-badge';
import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe, NgTemplateOutlet } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { ClientePublicoService } from './cliente-publico-service';
import { PedidoResponse, etiqueta } from '../pedidos/pedido.model';
import { mensajeHttp } from '../../shared/http-error';
@Component({
  selector: 'app-confirmar-cancelacion',
  imports: [MatDialogModule, MatButtonModule],
  template: `<h2 mat-dialog-title>¿Cancelar tu pedido?</h2>
    <mat-dialog-content>Esta acción cancela el pedido pendiente.</mat-dialog-content
    ><mat-dialog-actions
      ><button matButton [mat-dialog-close]="false" cdkFocusInitial>Conservar pedido</button
      ><button matButton="filled" [mat-dialog-close]="true">
        Sí, cancelar pedido
      </button></mat-dialog-actions
    >`,
})
export class ConfirmarCancelacion { }
@Component({
  selector: 'app-mi-pedido',
  imports: [ReactiveFormsModule, IconoCliente, SeguimientoPedido, EstadoBadge, CurrencyPipe, DatePipe, NgTemplateOutlet, NgTemplateOutlet, RouterLink],
  styleUrls: ['./publico.css', './seguimiento-premium.css'],
  templateUrl: './mi-pedido.html',
})
export class MiPedido {
  readonly publico = Boolean(inject(ActivatedRoute).snapshot.data?.['web']);
  private web = inject(WebCompraService);
  readonly claveRecuperacion = new FormControl('', { nonNullable: true });
  readonly claveGenerada = signal('');
  readonly avisoAcceso = signal('');
  generarClave() { if (this.ocupado()) return; this.ocupado.set(true); this.web.generarClave(this.id).subscribe({ next: r => { this.claveGenerada.set(r.clave); this.avisoAcceso.set('Guarda esta clave en un lugar privado. Es válida durante 30 días, se usa una sola vez y reemplaza cualquier clave anterior.'); this.ocupado.set(false); }, error: () => { this.error.set('No se pudo generar la clave de recuperación.'); this.ocupado.set(false); } }); }
  recuperar() { if (this.ocupado()) return; this.ocupado.set(true); this.web.recuperar(this.id, this.claveRecuperacion.value.trim()).subscribe({ next: p => { this.pedido.set(p); this.error.set(''); this.claveRecuperacion.reset(); this.ocupado.set(false); }, error: () => { this.error.set('No se pudo recuperar el acceso con esta clave.'); this.ocupado.set(false); } }); }
  revocarAcceso() { if (this.ocupado()) return; this.ocupado.set(true); this.web.revocar(this.id).subscribe({ next: () => { this.pedido.set(null); this.claveGenerada.set(''); this.error.set('Acceso de compra revocado. Las claves anteriores ya no son válidas.'); this.ocupado.set(false); }, error: () => { this.error.set('No se pudo revocar el acceso.'); this.ocupado.set(false); } }); }

  readonly pagoActualizado = () => this.cargar();
  readonly mostrarSeguimiento = signal(false);
  descripcionEstado(estado: string) {
    return estado === 'PENDIENTE'
      ? 'Nuestro equipo de cocina recibió tu pedido y lo está preparando.'
      : estado === 'LISTO'
        ? 'Tu pedido está listo para la entrega.'
        : estado === 'EN_CAMINO'
          ? 'Tu pedido está en camino con el repartidor.'
          : estado === 'ENTREGADO'
            ? 'Tu pedido fue entregado. Gracias por elegir Pollería Neyos.'
            : 'Consulta el estado registrado de tu pedido.';
  }
  readonly recienCreado = Boolean(
    inject(Router).currentNavigation()?.extras.state?.['pedidoCreado'],
  );
  private readonly api = inject(ClientePublicoService);
  private readonly dialog = inject(MatDialog);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  readonly pedido = signal<PedidoSeguimiento | null>(null);
  readonly error = signal('');
  readonly ocupado = signal(false);
  readonly etiqueta = etiqueta;
  constructor() {
    this.cargar();
  }
  cargar() {
    if (!Number.isSafeInteger(this.id) || this.id < 1) {
      this.error.set('Pedido no encontrado.');
      return;
    }
    this.ocupado.set(true);
    this.error.set('');
    (this.publico ? this.web.seguir(this.id) : this.api.pedido(this.id)).subscribe({
      next: (p) => {
        this.pedido.set(p);
        this.ocupado.set(false);
      },
      error: (e) => {
        this.error.set(this.publico ? 'No se pudo acceder al pedido. Conserva la credencial del navegador o utiliza tu clave de recuperación.' : mensajeHttp(e));
        this.ocupado.set(false);
      },
    });
  }
  cancelar() {
    if (this.publico || this.ocupado() || this.pedido()?.estado !== 'PENDIENTE') return;
    this.dialog
      .open(ConfirmarCancelacion, { width: '420px', maxWidth: '95vw' })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.ocupado.set(true);
          this.api.cancelar(this.id).subscribe({
            next: (p) => {
              this.pedido.set(p);
              this.ocupado.set(false);
            },
            error: (e) => {
              this.error.set(mensajeHttp(e));
              this.ocupado.set(false);
            },
          });
        }
      });
  }
}
