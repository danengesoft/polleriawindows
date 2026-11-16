import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
export interface ConfirmacionPedido {
  titulo: string;
  mensaje: string;
  lineas?: { nombre: string; cantidad: number; subtotal: number }[];
  total?: number;
}
@Component({
  selector: 'app-confirmar-pedido',
  imports: [MatDialogModule, MatButtonModule, CurrencyPipe],
  template: `<h2 mat-dialog-title>{{ data.titulo }}</h2>
    <mat-dialog-content
      ><p>{{ data.mensaje }}</p>
      @if (data.lineas) {
        <ul>
          @for (linea of data.lineas; track $index) {
            <li>
              {{ linea.cantidad }} × {{ linea.nombre }} —
              {{ linea.subtotal | currency: 'PEN' : 'S/ ' : '1.2-2' }}
            </li>
          }
        </ul>
      }
      @if (data.total !== undefined) {
        <p>
          Total informativo: {{ data.total | currency: 'PEN' : 'S/ ' : '1.2-2' }}. El servidor
          confirmará el importe final.
        </p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end"
      ><button matButton [mat-dialog-close]="false" cdkFocusInitial>Volver</button
      ><button matButton="filled" [mat-dialog-close]="true">Confirmar</button></mat-dialog-actions
    >`,
})
export class ConfirmarPedido {
  readonly data = inject<ConfirmacionPedido>(MAT_DIALOG_DATA);
}
