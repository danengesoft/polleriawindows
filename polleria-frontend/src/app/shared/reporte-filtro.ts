import { HttpParams } from '@angular/common/http';
export interface ReporteFiltro {
  fechaDesde?: string;
  fechaHasta?: string;
  top?: number;
}
export function parametrosReporte(f: ReporteFiltro) {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(f))
    if (value !== undefined && value !== '') params = params.set(key, String(value));
  return params;
}
