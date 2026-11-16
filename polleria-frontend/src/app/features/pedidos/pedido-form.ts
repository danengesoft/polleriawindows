import { MapaMesas } from '../operacion/mapa-mesas';
import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog } from '@angular/material/dialog';
import { map } from 'rxjs';
import { ClienteService } from '../clientes/cliente/cliente-service';
import { Cliente } from '../clientes/cliente/cliente.model';
import { ProductoService } from '../catalogo/producto/producto-service';
import { Producto } from '../catalogo/producto/producto.model';
import { mensajeHttp } from '../../shared/http-error';
import { textoObligatorio, mensajeValidacion } from '../../shared/form-validation';
import { PedidoService } from './pedido-service';
import { ConfirmarPedido, ConfirmacionPedido } from './confirmar-pedido';
import { METODOS, TIPOS, MetodoPago, PedidoRequest, TipoPedido, etiqueta } from './pedido.model';

export const cantidadEntera: ValidatorFn = (control) =>
  Number.isInteger(control.value) && control.value > 0 && control.value <= 2147483647
    ? null
    : { cantidad: true };
export const detallesValidos: ValidatorFn = (control) => {
  const values: { productoId?: number;presentacionId?:number }[] = control.value ?? [];
  if (!values.length) return { sinDetalles: true };
  const ids = values.filter(v=>(v.productoId??0)>0).map(value=>value.productoId+':'+(value.presentacionId||0));
  return new Set(ids).size === ids.length ? null : { duplicados: true };
};
@Component({
  selector: 'app-pedido-form',
  imports: [
    MapaMesas,
    ReactiveFormsModule,
    RouterLink,
    CurrencyPipe,
    MatAutocompleteModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './pedido-form.html',
  styleUrl: './pedido.css',
})
export class PedidoForm {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly productoService = inject(ProductoService);
  private readonly service = inject(PedidoService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly clientes = signal<Cliente[]>([]);
  readonly productos = signal<Producto[]>([]);
  readonly clientesCargados = signal(false);
  readonly productosCargados = signal(false);
  readonly errorClientes = signal('');
  readonly errorProductos = signal('');
  readonly error = signal('');
  readonly guardando = signal(false);
  readonly confirmando = signal(false);
  readonly tipos = TIPOS;
  readonly metodos = METODOS;
  readonly etiqueta = etiqueta;
  readonly validacion = mensajeValidacion;
  readonly form = this.fb.nonNullable.group({
    mesaId: [0],
    clienteId: [0, [Validators.min(1)]],
    tipoPedido: ['PARA_LLEVAR' as TipoPedido, [Validators.required]],
    metodoPago: ['EFECTIVO' as MetodoPago, [Validators.required]],
    direccionEntrega: ['', [Validators.maxLength(300)]],
    referenciaEntrega: ['', [Validators.maxLength(300)]],
    detalles: this.fb.array([this.crearLinea()], [detallesValidos]),
  });
  readonly valoresDetalles = toSignal(
    this.form.controls.detalles.valueChanges.pipe(
      map(() => this.form.controls.detalles.getRawValue()),
    ),
    { initialValue: this.form.controls.detalles.getRawValue() },
  );
  readonly tipoSeleccionado = toSignal(this.form.controls.tipoPedido.valueChanges, {
    initialValue: this.form.controls.tipoPedido.value,
  });
  readonly preciosUnitarios = computed(() =>
    this.valoresDetalles().map(
      (linea) => {const p=this.productos().find(p=>p.id===linea.productoId);return p?.presentaciones?.find(x=>x.id===linea.presentacionId)?.precio??p?.precio??0;},
    ),
  );
  // Solo estimación de UX. No se incluye ningún importe en PedidoRequest.
  readonly subtotales = computed(() =>
    this.valoresDetalles().map((linea, i) =>
      Number.isInteger(linea.cantidad) && linea.cantidad > 0
        ? (Math.round(this.preciosUnitarios()[i] * 100) * linea.cantidad) / 100
        : 0,
    ),
  );
  readonly total = computed(
    () => this.subtotales().reduce((suma, subtotal) => suma + Math.round(subtotal * 100), 0) / 100,
  );
  readonly opcionesPorLinea = computed(() =>
    this.valoresDetalles().map((linea, indice) =>
      this.productos().filter(
        (p) =>
          p.nombre.toLowerCase().includes(linea.busqueda.toLowerCase()) &&
          ((p.presentaciones?.length||0)>1||!this.valoresDetalles().some((otra, j) => j !== indice && otra.productoId === p.id)),
      ),
    ),
  );
  constructor() {
    this.cargarClientes();
    this.cargarProductos();
    this.form.controls.tipoPedido.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((tipo) => {
        this.form.controls.direccionEntrega.setValidators([
          Validators.maxLength(300),

        ]);
        this.form.controls.direccionEntrega.updateValueAndValidity();
      });
  }
  private crearLinea() {
    return this.fb.nonNullable.group({
      productoId: [0, [Validators.min(1)]],
      presentacionId:[0],
      busqueda: [''],
      cantidad: [1, [Validators.required, cantidadEntera]],
    });
  }
  get lineasForm() {
    return this.form.controls.detalles;
  }
  agregarLinea() {
    if (!this.guardando() && !this.confirmando()) this.lineasForm.push(this.crearLinea());
  }
  quitarLinea(indice: number) {
    if (!this.guardando() && !this.confirmando() && this.lineasForm.length > 1)
      this.lineasForm.removeAt(indice);
  }
  agregarDesdeCarta(id:number){
    if(this.guardando()||this.confirmando())return;
    const existente=this.lineasForm.controls.find(l=>l.controls.productoId.value===id);
    if(existente&&(this.productos().find(p=>p.id===id)?.presentaciones?.length||0)<=1){existente.controls.cantidad.setValue(existente.controls.cantidad.value+1);return;}
    const vacia=this.lineasForm.controls.findIndex(l=>l.controls.productoId.value===0);
    if(vacia>=0)this.seleccionarProducto(vacia,id);
    else{this.agregarLinea();this.seleccionarProducto(this.lineasForm.length-1,id);}
  }
  buscarProducto(indice: number) {
    this.lineasForm.at(indice).controls.productoId.setValue(0);
  }
  seleccionarProducto(indice: number, id: number) {
    const producto = this.productos().find((p) => p.id === id);
    if (
      !producto || !producto.disponible ||
      this.lineasForm.controls.some(
        (linea, j) => j !== indice && linea.controls.productoId.value === id && (producto.presentaciones?.length||0)<=1,
      )
    )
      return;
    this.lineasForm.at(indice).patchValue({ productoId: id, busqueda: producto.nombre,presentacionId:producto.presentaciones?.filter(p=>p.disponible).length===1?producto.presentaciones.filter(p=>p.disponible)[0].id||0:0 });
  }
  presentacionesDe(id:number){return this.productos().find(p=>p.id===id)?.presentaciones?.filter(p=>p.disponible)||[];}
  cargarClientes() {
    this.clientesCargados.set(false);
    this.errorClientes.set('');
    this.clienteService
      .listar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.clientes.set(data);
          this.clientesCargados.set(true);
        },
        error: (err) => this.errorClientes.set(mensajeHttp(err)),
      });
  }
  cargarProductos() {
    this.productosCargados.set(false);
    this.errorProductos.set('');
    this.productoService
      .listar()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.productos.set(data);
          this.productosCargados.set(true);
        },
        error: (err) => this.errorProductos.set(mensajeHttp(err)),
      });
  }
  confirmarYGuardar() {
    if (
      this.guardando() ||
      this.confirmando() ||
      !this.clientesCargados() ||
      !this.productosCargados()
    )
      return;
    this.error.set('');
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    if(value.tipoPedido==='MESA' && value.mesaId<=0){this.error.set('Selecciona una mesa disponible.');return;}
    if (!this.clientes().some((c) => c.id === value.clienteId)) {
      this.error.set('Selecciona un cliente existente.');
      return;
    }
    if (value.detalles.some((linea) => !this.productos().some((p) => p.id === linea.productoId && p.disponible))) {
      this.error.set('Selecciona productos disponibles de la lista.');
      return;
    }
    if(value.detalles.some(l=>this.presentacionesDe(l.productoId).length>1&&!l.presentacionId)){this.error.set('Selecciona la presentación de cada producto.');return;}
    const request: PedidoRequest = {
      clienteId: value.clienteId,
      ...(value.tipoPedido==='MESA'?{mesaId:value.mesaId}:{}),
      tipoPedido: value.tipoPedido,
      metodoPago: value.metodoPago,
      direccionEntrega:
        null,
      referenciaEntrega:
        null,
      detalles: value.detalles.map((linea) => ({
        productoId: linea.productoId,
        cantidad: linea.cantidad,
        ...(linea.presentacionId?{presentacionId:linea.presentacionId}:{}),
      })),
    };
    const cliente = this.clientes().find((c) => c.id === value.clienteId)!;
    const data: ConfirmacionPedido = {
      titulo: 'Confirmar pedido',
      mensaje: `${cliente.nombres} ${cliente.apellidos} · ${etiqueta(value.tipoPedido)} · ${etiqueta(value.metodoPago)}`,
      total: this.total(),
      lineas: value.detalles.map((linea, i) => ({
        nombre: this.productos().find((p) => p.id === linea.productoId)!.nombre+(linea.presentacionId?' · '+this.presentacionesDe(linea.productoId).find(p=>p.id===linea.presentacionId)?.nombre:''),
        cantidad: linea.cantidad,
        subtotal: this.subtotales()[i],
      })),
    };
    this.confirmando.set(true);
    this.form.disable({ emitEvent: false });
    this.dialog
      .open(ConfirmarPedido, { data, width: '34rem', maxWidth: 'calc(100vw - 2rem)' })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        this.confirmando.set(false);
        if (confirmado !== true) {
          this.form.enable({ emitEvent: false });
          return;
        }
        this.guardando.set(true);
        this.service
          .crear(request)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (pedido) =>
              this.router.navigate(['/interno/pedidos', pedido.id], {
                state: { pedidoCreado: pedido },
              }),
            error: (err) => {
              this.guardando.set(false);
              this.form.enable({ emitEvent: false });
              this.error.set(mensajeHttp(err));
              if ([404, 409].includes(err.status)) {
                this.cargarClientes();
                this.cargarProductos();
              }
            },
          });
      });
  }
}
