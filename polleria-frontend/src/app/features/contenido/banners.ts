import { PromocionesAdmin } from './promociones';
import { DestacadosAdmin } from './destacados';
import { MatDialog, MatDialogRef, MatDialogTitle, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { Component, inject, signal, computed, DestroyRef, ViewChild, TemplateRef } from '@angular/core';
import { UbicacionAdmin } from './ubicacion';
import { FormsModule } from '@angular/forms';
import { BannersService, Banner, BANNER_ORIGINAL } from './banners-service';
import { ProductoService } from '../catalogo/producto/producto-service';
import { mensajeHttp } from '../../shared/http-error';
@Component({ selector: 'app-banners', imports: [FormsModule, PromocionesAdmin, UbicacionAdmin, DestacadosAdmin, MatDialogTitle, MatDialogContent, MatDialogActions], templateUrl: './banners.html', styleUrl: './banners.css' })
export class Banners {
    @ViewChild('editor') editor?: TemplateRef<unknown>;
    private dialog = inject(MatDialog); private dialogRef?: MatDialogRef<unknown>; readonly seleccionada = signal<Banner | null>(null); readonly vista = signal<'fotos' | 'ubicacion' | 'destacados' | 'promociones'>('fotos');
    abrirEditor(b: Banner) { this.seleccionada.set(b); if (this.editor) this.dialogRef = this.dialog.open(this.editor, { width: '520px', maxWidth: 'calc(100vw - 24px)', maxHeight: '90dvh' }); }
    cerrarEditor() { if (!this.ocupado()) this.dialogRef?.close(); }
    readonly pagina = signal(0); readonly porPagina = signal(window.innerWidth <= 700 ? 2 : window.innerWidth <= 1000 ? 3 : 5);
    readonly visibles = computed(() => this.fotos().map((foto, indice) => ({ foto, indice })).slice(this.pagina() * this.porPagina(), (this.pagina() + 1) * this.porPagina()));
    readonly api = inject(BannersService); private imagenes = inject(ProductoService); readonly fotos = signal<Banner[]>([]); readonly listo = signal(false); readonly cargando = signal(false); readonly ocupado = signal(false); readonly error = signal(''); readonly aviso = signal('');
    constructor() { this.cargar(); const ajustar = () => { this.porPagina.set(window.innerWidth <= 700 ? 2 : window.innerWidth <= 1000 ? 3 : 5); this.pagina.set(0); }; window.addEventListener('resize', ajustar); inject(DestroyRef).onDestroy(() => window.removeEventListener('resize', ajustar)); }
    cargar() { if (this.cargando()) return; this.cargando.set(true); this.error.set(''); this.api.gestion().subscribe({ next: r => { this.fotos.set((r.banners.length ? r.banners : [BANNER_ORIGINAL]).map(b => ({ ...b, activo: true, imagenMovilUrl: null }))); this.listo.set(true); this.cargando.set(false); }, error: e => { this.error.set(mensajeHttp(e)); this.cargando.set(false); } }); }
    agregar() { this.fotos.update(v => [...v, { id: crypto.randomUUID(), titulo: 'Fotografía de Pollería Neyos', imagenUrl: '', imagenMovilUrl: null, activo: true }]); this.aviso.set('Carga la fotografía y guarda la portada para publicarla.'); this.pagina.set(Math.floor((this.fotos().length - 1) / this.porPagina())); this.abrirEditor(this.fotos()[this.fotos().length - 1]); }
    mover(i: number, paso: number) { this.fotos.update(v => { const a = [...v];[a[i], a[i + paso]] = [a[i + paso], a[i]]; return a; }); }
    subir(event: Event, b: Banner) { const input = event.target as HTMLInputElement; const f = input.files?.[0]; if (!f || this.ocupado()) return; this.error.set(''); if (f.size > 20 * 1024 * 1024 || !['image/jpeg', 'image/png'].includes(f.type)) { this.error.set('Selecciona JPG o PNG de hasta 20 MB.'); input.value = ''; return; } this.ocupado.set(true); this.imagenes.cargarImagen(f).subscribe({ next: r => { const ruta = new URL(r.imagenUrl).pathname; b.imagenUrl = ruta; b.imagenMovilUrl = null; this.ocupado.set(false); input.value = ''; this.aviso.set('Fotografía cargada. Guarda la portada para aplicarla.'); }, error: e => { this.error.set(mensajeHttp(e)); this.ocupado.set(false); input.value = ''; } }); }
    guardar() { if (this.ocupado() || !this.listo()) return; if (this.fotos().some(b => !b.imagenUrl || !b.titulo.trim())) { this.error.set('Cada foto necesita un nombre y una fotografía.'); return; } this.error.set(''); this.ocupado.set(true); this.api.guardar(this.fotos().map(b => ({ ...b, activo: true, imagenMovilUrl: null }))).subscribe({ next: r => { this.fotos.set(r.banners.map(b => ({ ...b }))); this.ocupado.set(false); this.aviso.set('Portada guardada. Ya está disponible en la página de inicio.'); }, error: e => { this.ocupado.set(false); this.error.set(mensajeHttp(e)); } }); }
}
