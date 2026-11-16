import { MatDialog } from '@angular/material/dialog';
import { AccesoModalService } from '../../../features/publico/acceso-dialog';
import { UbicacionDialog } from '../../../features/contenido/ubicacion';
import { Component, input, inject, signal, ViewEncapsulation } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService, INTERNOS } from '../../auth/auth-service';
import { CarritoService } from '../../../features/publico/carrito-service';
import { IconoCliente } from '../../../features/publico/icono-cliente';
@Component({
  selector: 'app-cliente-shell',
  imports: [RouterLink, RouterLinkActive, IconoCliente, CurrencyPipe, ReactiveFormsModule],
  encapsulation: ViewEncapsulation.None,
  templateUrl: './cliente-shell.html',
  styleUrls: [
    './cliente-base.css',
    './cliente-controles.css',
    './cliente-tarjetas.css',
    './cliente-responsive.css',
    './cliente-movil.css',
    './cliente-amplio.css',
    './cliente-accesos.css',
  ],
})
export class ClienteShell {
  private router = inject(Router); readonly busqueda = new FormControl('', { nonNullable: true }); readonly buscarAbierto = signal(false);
  buscar(event: Event) { event.preventDefault(); this.router.navigate(['/catalogo'], { queryParams: { q: this.busqueda.value.trim().slice(0, 120) || null } }); this.buscarAbierto.set(false); }
  readonly acceso = inject(AccesoModalService); private dialog = inject(MatDialog);
  abrirUbicacion() { this.dialog.open(UbicacionDialog, { width: '460px', maxWidth: 'calc(100vw - 28px)', autoFocus: 'first-tabbable' }); }
  readonly auth = inject(AuthService);
  readonly carrito = inject(CarritoService);
  readonly amplio = input(false);
  readonly menuAbierto = signal(false);
  readonly internos = INTERNOS;
  cerrarMenu(boton?: HTMLButtonElement) {
    this.menuAbierto.set(false);
    boton?.focus();
  }
}
