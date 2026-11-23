// Modelos del módulo clínica: lo que el backend devuelve (Response) y lo que recibe (Request).

export interface Clinica {
  id: number;
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface ClinicaRequest {
  ruc: string;
  razonSocial: string;
  telefono: string | null;
  activo: boolean;
}

export interface Especialidad {
  id: number;
  codigo: string;
  nombre: string;
}

export const ESTADOS_CONSULTORIO = ['DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO'] as const;
export type EstadoConsultorio = (typeof ESTADOS_CONSULTORIO)[number];

// Cabecera (consultorio) con sus líneas (especialidades).
export interface Consultorio {
  id: number;
  clinicaId: number;
  clinicaRazonSocial: string;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidades: Especialidad[];
}

export interface ConsultorioRequest {
  clinicaId: number;
  codigo: string;
  tarifaHora: number;
  estado: EstadoConsultorio;
  especialidadIds: number[];
}
