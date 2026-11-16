import { FotoProducto } from './foto-producto';
import { IconoCliente } from './icono-cliente';
import { ProductoService } from '../catalogo/producto/producto-service';
import { Producto } from '../catalogo/producto/producto.model';
import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { CarritoService } from './carrito-service';
@Component({
  selector: 'app-confirmar-vaciado',
  imports: [MatDialogModule, MatButtonModule],
  template: `<h2 mat-dialog-title>¿Vaciar el carrito?</h2>
    <mat-dialog-content>Se quitarán los productos de tu selección.</mat-dialog-content
    ><mat-dialog-actions align="end"
      ><button matButton [mat-dialog-close]="false" cdkFocusInitial>Conservar carrito</button
      ><button matButton="filled" [mat-dialog-close]="true">
        Vaciar carrito
      </button></mat-dialog-actions
    >`,
})
export class ConfirmarVaciado { }
@Component({
  selector: 'app-carrito',
  imports: [CurrencyPipe, RouterLink, FotoProducto, IconoCliente],
  styleUrl: './publico.css',
  templateUrl: './carrito.html',
})
export class Carrito {
  readonly carrito = inject(CarritoService);
  readonly productos = signal<Producto[]>([]);
  private readonly productosApi = inject(ProductoService);
  constructor() {
    if (this.carrito.lineas().length)
      this.productosApi.listar().subscribe({ next: (p) => this.productos.set(p), error: () => { } });
  }
  foto(id: number) {
    return this.productos().find((p) => p.id === id)?.imagenUrl || null;
  }
  private readonly dialog = inject(MatDialog);
  confirmarVaciado() {
    this.dialog
      .open(ConfirmarVaciado, { width: '420px', maxWidth: '95vw' })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) this.carrito.vaciar();
      });
  }
}
