import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-confirmar-eliminacion',
  imports: [MatDialogModule, MatButtonModule],
  template: `<h2 mat-dialog-title>Confirmar eliminación</h2>
    <mat-dialog-content
      >¿Eliminar {{ nombre }}? Esta acción no se puede deshacer.</mat-dialog-content
    >
    <mat-dialog-actions align="end">
      <button matButton [mat-dialog-close]="false" cdkFocusInitial>Cancelar</button>
      <button matButton="filled" [mat-dialog-close]="true">Confirmar eliminación</button>
    </mat-dialog-actions>`,
})
export class ConfirmarEliminacion {
  readonly nombre = inject<string>(MAT_DIALOG_DATA);
}
