import { inject } from '@angular/core';
import { CanActivateFn, CanActivateChildFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth-service';
export const authGuard: CanActivateFn = async (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (await auth.asegurarSesion()) return true;
  return router.parseUrl('/acceso');
};
export const authChildGuard: CanActivateChildFn = authGuard;
