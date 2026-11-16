import { IconoPortal } from '../../../shared/icono-portal';
import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../auth/auth-service';
import { AppNavigation } from '../../../shared/navigation/app-navigation';

@Component({
  selector: 'app-layout-interno',
  imports: [RouterLinkActive, RouterLink, RouterOutlet, AppNavigation, IconoPortal],
  templateUrl: './interno.html',
  styleUrl: './interno.css',
})
export class Interno {
  cerrarMenu(boton: HTMLButtonElement) {
    this.menuAbierto.set(false);
    if (boton.offsetWidth > 0) boton.focus();
  }
  readonly auth = inject(AuthService);
  readonly menuAbierto = signal(false);
}
