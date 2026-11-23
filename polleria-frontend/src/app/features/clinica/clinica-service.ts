import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import {
  Clinica,
  ClinicaRequest,
  Consultorio,
  ConsultorioRequest,
  Especialidad,
} from './clinica.model';

// Un solo servicio para las tres pantallas del módulo: /api/v1/clinica/...
@Injectable({ providedIn: 'root' })
export class ClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly clinicas = '/api/v1/clinica/clinicas';
  private readonly consultorios = '/api/v1/clinica/consultorios';

  listarClinicas() {
    return this.http.get<Clinica[]>(this.api.buildUrl(this.clinicas));
  }
  obtenerClinica(id: number) {
    return this.http.get<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
  crearClinica(data: ClinicaRequest) {
    return this.http.post<Clinica>(this.api.buildUrl(this.clinicas), data);
  }
  actualizarClinica(id: number, data: ClinicaRequest) {
    return this.http.put<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`), data);
  }
  eliminarClinica(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }

  listarEspecialidades() {
    return this.http.get<Especialidad[]>(this.api.buildUrl('/api/v1/clinica/especialidades'));
  }

  listarConsultorios() {
    return this.http.get<Consultorio[]>(this.api.buildUrl(this.consultorios));
  }
  obtenerConsultorio(id: number) {
    return this.http.get<Consultorio>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
  crearConsultorio(data: ConsultorioRequest) {
    return this.http.post<Consultorio>(this.api.buildUrl(this.consultorios), data);
  }
  eliminarConsultorio(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
}
