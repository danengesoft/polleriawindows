import { Component, effect, inject, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import qrcode from 'qrcode-generator';
import { WebCompraService } from './web-compra-service';
export interface PagoPrueba { modo: string; pedidoId: number; metodo: string; monto: number; moneda: string; estadoPago: string; resultado: string; qrTexto: string }
@Component({
    selector: 'app-pago-prueba', imports: [CurrencyPipe], template: `
 @if(pago();as p){<section class="pago-demo" aria-label="Simulador local de pago"><span class="demo-etiqueta">PAGO DE PRUEBA · SIN COBRO REAL</span><h2>{{p.metodo==='TARJETA'?'Tarjeta de prueba':'QR de prueba · '+p.metodo}}</h2><p>Monto del pedido calculado por el servidor: <strong>{{p.monto|currency:'PEN':'S/ ':'1.2-2'}}</strong></p>
 @if(p.metodo!=='TARJETA'){<img [src]="qr()" alt="QR de simulación local, no válido para pagos reales" width="220" height="220" /><p>Este QR contiene la referencia y el monto de prueba. No se puede pagar con una billetera real.</p>}
 @else{<p>Usa los controles del entorno de pruebas. No ingreses números de tarjeta ni datos bancarios reales.</p>}
 <p role="status">{{aviso()}}</p><div class="actions"><button type="button" [disabled]="ocupado()||p.estadoPago==='PAGADO'" (click)="resolver('APROBAR')">Simular pago aprobado</button><button class="secondary" type="button" [disabled]="ocupado()||p.estadoPago==='PAGADO'" (click)="resolver('RECHAZAR')">Simular pago rechazado</button></div></section>}
 @if(error()){<p role="alert">{{error()}}</p>}
 `, styles: `.pago-demo{padding:clamp(1rem,3vw,2rem);border:2px dashed var(--gold,var(--accent));border-radius:12px;margin:1.5rem 0;background:var(--surface)}.demo-etiqueta{font-size:.75rem;letter-spacing:.08em;font-weight:bold;color:var(--gold,var(--accent))}.pago-demo img{display:block;max-width:100%;height:auto;background:white;padding:10px;margin:1rem 0}.pago-demo h2{margin-top:1rem}.pago-demo p{font-size:.9rem}`
})
export class PagoPruebaPanel {
    private web = inject(WebCompraService); readonly interno = input(false); readonly pedidoId = input.required<number>(); readonly actualizado = input<() => void>(() => { }); readonly pago = signal<PagoPrueba | null>(null); readonly qr = signal(''); readonly aviso = signal(''); readonly error = signal(''); readonly ocupado = signal(false);
    constructor() { effect(onCleanup => { const id = this.pedidoId(); const sub = (this.interno() ? this.web.pagoPruebaInterno(id, 'CONSULTAR') : this.web.pagoPrueba(id, 'CONSULTAR')).subscribe({ next: p => this.mostrar(p), error: e => { if (e.status !== 404) this.error.set('No se pudo consultar el entorno de pago de prueba.'); } }); onCleanup(() => sub.unsubscribe()); }); }
    private mostrar(p: PagoPrueba) { this.pago.set(p); if (p.metodo !== 'TARJETA') { const code = qrcode(0, 'M'); code.addData(p.qrTexto); code.make(); this.qr.set(code.createDataURL(5, 20)); } this.aviso.set(p.estadoPago === 'PAGADO' ? 'Pago aprobado en el simulador. No se realizó ningún cobro real.' : p.resultado === 'RECHAZADO' ? 'Pago de prueba rechazado. El pedido continúa pendiente de pago.' : 'Esperando la respuesta del simulador local.'); }
    resolver(resultado: 'APROBAR' | 'RECHAZAR') { if (this.ocupado()) return; this.ocupado.set(true); this.error.set(''); (this.interno() ? this.web.pagoPruebaInterno(this.pedidoId(), resultado) : this.web.pagoPrueba(this.pedidoId(), resultado)).subscribe({ next: p => { this.mostrar(p); this.ocupado.set(false); if (p.estadoPago === 'PAGADO') this.actualizado()(); }, error: () => { this.error.set('No se pudo procesar la prueba. El pago no se da por confirmado.'); this.ocupado.set(false); } }); }
}
