import { OfertaPromocion } from '../contenido/promociones-service';
import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { Producto,Presentacion } from '../catalogo/producto/producto.model';
import { CarritoService } from './carrito-service';
import { FotoProducto } from './foto-producto';
import { IconoCliente } from './icono-cliente';
@Component({
  selector: 'app-producto-detalle-publico',
  imports: [CurrencyPipe, FotoProducto, IconoCliente, MatDialogModule],
  template: `<div class="detail-photo">
      <app-foto-producto [url]="producto.imagenUrl" [nombre]="producto.nombre" /><button
        class="close"
        type="button"
        mat-dialog-close
        aria-label="Cerrar detalle"
      >
        <app-icono-cliente nombre="cerrar" />
      </button>
    </div>
    <div class="detail-copy">
      <p class="category">{{ producto.categoria.nombre }}</p>
      <h2 mat-dialog-title>{{ producto.nombre }}</h2>
      <strong class="price">{{ precio() | currency: 'PEN' : 'S/ ' : '1.2-2' }}</strong>
      @if(oferta){<p class="promotion-price">Promoción · -{{oferta.porcentaje}}% · Antes <s>{{oferta.precioRegular|currency:'PEN':'S/ ':'1.2-2'}}</s></p>@if(seleccion();as p){<p>{{p.nombre}}</p>}}
      <p>
        {{ producto.descripcion || 'Consulta la presentación de este plato en nuestra carta.' }}
      </p>
      <p class="note">
        El producto se ofrece según su presentación en la carta. El precio y la disponibilidad se
        confirman al crear el pedido.
      </p>
      @if(opciones().length>1){<fieldset><legend>Elige la presentación</legend>@for(p of opciones();track p.id){<label style="display:flex;gap:10px;padding:10px"><input type="radio" name="presentacion" [checked]="seleccion()?.id===p.id" (change)="seleccion.set(p)"/>{{p.nombre}} · {{p.precio|currency:'PEN':'S/ ':'1.2-2'}}</label>}</fieldset>}
      <div class="detail-actions">
        <div class="quantity">
          <button
            (click)="cantidad.set(cantidad() - 1)"
            [disabled]="cantidad() <= 1"
            aria-label="Disminuir cantidad"
          >
            −</button
          ><output aria-live="polite">{{ cantidad() }}</output
          ><button
            (click)="cantidad.set(cantidad() + 1)"
            [disabled]="cantidad() >= limite()"
            aria-label="Aumentar cantidad"
          >
            +
          </button>
        </div>
        <button class="add" (click)="agregar()" [disabled]="!producto.disponible || limite() < 1 || (opciones().length>1&&!seleccion())">
          {{ producto.disponible ? 'Agregar al carrito' : 'No disponible' }} ·
          {{ precio() * cantidad() | currency: 'PEN' : 'S/ ' : '1.2-2' }}
        </button>
      </div>
    </div>`,
  styleUrl: './producto-detalle.css',
})
export class ProductoDetallePublico {
  readonly producto = inject<Producto & {oferta?:OfertaPromocion}>(MAT_DIALOG_DATA);
  readonly oferta=this.producto.oferta;
  precio(){return this.oferta?.precioPromocional??this.seleccion()?.precio??this.producto.precio;}
  private ref = inject(MatDialogRef<ProductoDetallePublico>);
  private carrito = inject(CarritoService);
  opciones(){return this.producto.presentaciones?.filter(p=>p.disponible&&(!this.oferta||!this.oferta.presentacionId||p.id===this.oferta.presentacionId))||[];}
  readonly seleccion=signal<Presentacion|undefined>(this.opciones().length===1?this.opciones()[0]:undefined);
  readonly cantidad = signal(1);
  limite() {
    return (
      999 - (this.carrito.lineas().find((l) => l.productoId === this.producto.id&&l.presentacionId===this.seleccion()?.id&&l.promocionId===this.oferta?.id)?.cantidad || 0)
    );
  }
  agregar() {
    if(this.opciones().length>1&&!this.seleccion())return;
    if (!this.producto.disponible || this.cantidad() > this.limite() || this.cantidad() < 1) return;
    for (let i = 0; i < this.cantidad(); i++) {if(this.oferta)this.carrito.agregarPromocion(this.producto,this.oferta);else this.carrito.agregar(this.producto,this.seleccion());}
    this.ref.close(true);
  }
}
