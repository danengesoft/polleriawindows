export interface JwtPayload {
  sub: string;
  email?: string;
  email_verified?: boolean;
  exp: number;
  iss: string;
  name?: string;
  preferred_username?: string;
  realm_access?: { roles?: string[] };
}
// Lectura para UX; la validación de firma y autorización siguen en Spring.
export function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const partes = token.split('.');
    if (partes.length !== 3) return null;
    const raw: unknown = JSON.parse(
      new TextDecoder().decode(
        Uint8Array.from(atob(partes[1].replace(/-/g, '+').replace(/_/g, '/')), (c) =>
          c.charCodeAt(0),
        ),
      ),
    );
    if (
      typeof raw !== 'object' ||
      raw === null ||
      !('sub' in raw) ||
      typeof raw.sub !== 'string' ||
      !('exp' in raw) ||
      typeof raw.exp !== 'number' ||
      !Number.isFinite(raw.exp) ||
      !('iss' in raw) ||
      typeof raw.iss !== 'string'
    )
      return null;
    return raw as JwtPayload;
  } catch {
    return null;
  }
}
