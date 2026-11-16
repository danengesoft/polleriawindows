import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Rol, INTERNOS } from '../auth/auth-service';
export function rolGuard(...roles: Rol[]): CanActivateFn {
  return () => inject(AuthService).tieneRol(...roles) || inject(Router).parseUrl('/sin-permiso');
}

export function destinoInterno(auth: AuthService): string {
  if (auth.tieneRol('ADMINISTRADOR', 'OPERACIONES')) return '/interno/operaciones';
  if (auth.tieneRol('CAJERO')) return '/interno/caja';
  if (auth.tieneRol('MESERO')) return '/interno/mesero';
  return '/sin-permiso';
}
export const inicioInternoGuard: CanActivateFn = () => inject(Router).parseUrl(destinoInterno(inject(AuthService)));
/** Los roles internos tienen prioridad incluso cuando Keycloak incluye CLIENTE. */
export const publicoGuard: CanActivateFn = async () => {
  const auth = inject(AuthService), router = inject(Router);
  await auth.asegurarSesion();
  return auth.tieneRol(...INTERNOS) ? router.parseUrl(destinoInterno(auth)) : true;
};
