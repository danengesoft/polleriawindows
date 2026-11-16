export interface ClienteRequest {
  nombres: string;
  apellidos: string;
  telefono: string;
  correo: string | null;
  direccionPrincipal: string | null;
  referencia: string | null;
  activo: boolean;
}
export interface Cliente extends ClienteRequest {
  id: number;
  eliminable?: boolean;
}
