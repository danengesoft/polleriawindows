import { HttpErrorResponse } from '@angular/common/http';

export function mensajeHttp(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return 'No se pudo completar la operación.';
  if (error.status === 0) return 'No se pudo conectar con el servidor. Intenta nuevamente.';
  const body: unknown = error.error;
  if (
    [400, 404, 409].includes(error.status) &&
    typeof body === 'object' &&
    body !== null &&
    'message' in body &&
    typeof body.message === 'string'
  ) {
    return body.message;
  }
  if (error.status === 400) return 'Revisa los datos enviados.';
  if (error.status === 404) return 'El recurso solicitado ya no existe.';
  if (error.status === 409) return 'La operación entra en conflicto con datos existentes.';
  return 'No se pudo completar la operación. Intenta nuevamente más tarde.';
}
