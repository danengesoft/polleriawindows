import { Injectable, inject, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';
/** Identidad única oscura; ignora las antiguas preferencias claras. */
@Injectable({ providedIn: 'root' })
export class TemaService {
    private documento = inject(DOCUMENT);
    readonly preferencia = signal<'oscuro'>('oscuro');
    constructor() { this.cambiar('oscuro'); }
    cambiar(_valor: string) { this.documento.documentElement.dataset['tema'] = 'oscuro'; try { localStorage.setItem('polleria-neyos-tema', 'oscuro'); } catch {/* El tema no depende del almacenamiento. */ } }
}
