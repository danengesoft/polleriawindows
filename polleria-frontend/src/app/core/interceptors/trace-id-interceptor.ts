import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

// Trazabilidad S07; no es un interceptor de autenticación.
export const traceIdInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(`${environment.apiBaseUrl}/api/`)) return next(req);
  return next(req.clone({ setHeaders: { 'X-Trace-ID': crypto.randomUUID() } }));
};
