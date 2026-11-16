import { Injectable, InjectionToken, computed, inject, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { generarCodeChallenge, generarCodeVerifier, generarState } from './pkce';
import { decodeJwtPayload } from './jwt';

export const OAUTH_REDIRECT = new InjectionToken<(url: string) => void>('OAuth browser redirect', {
  providedIn: 'root',
  factory: () => (url) => location.assign(url),
});
export const ROLES = [
  'ADMINISTRADOR',
  'CAJERO',
  'CLIENTE',
  'OPERACIONES',
  'MESERO',
] as const;
export type Rol = (typeof ROLES)[number];
export const INTERNOS: Rol[] = ['ADMINISTRADOR', 'CAJERO', 'OPERACIONES', 'MESERO'];
export const OPERATIVOS: Rol[] = ['ADMINISTRADOR', 'CAJERO'];
export const AUTH_KEYS = {
  sesion: 'polleria_sesion',
  state: 'polleria_pkce_state',
  verifier: 'polleria_pkce_verifier',
  destino: 'polleria_pkce_destino',
} as const;
interface Sesion {
  accessToken: string;
  refreshToken: string;
  idToken: string;
  expiraEn: number;
  refreshExpiraEn: number;
}
interface TokenResponse {
  access_token: string;
  refresh_token: string;
  id_token: string;
  expires_in: number;
  refresh_expires_in: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly redirigir = inject(OAUTH_REDIRECT);
  private readonly sesion = signal<Sesion | null>(this.restaurarSesion());
  private readonly reloj = signal(Date.now());
  private temporizador?: ReturnType<typeof setTimeout>;
  private renovacion: Promise<boolean> | null = null;
  private loginEnCurso = false;
  readonly error = signal('');
  readonly autenticado = computed(() => {
    const s = this.sesion();
    return !!s && s.expiraEn > this.reloj();
  });
  readonly roles = computed<Rol[]>(() => {
    const token = this.sesion()?.accessToken;
    const claims = token ? decodeJwtPayload(token) : null;
    const roles = claims?.realm_access?.roles;
    return Array.isArray(roles) ? ROLES.filter((role) => roles.includes(role) && (role === 'CLIENTE' || (claims?.email_verified === true && !!claims.email))) : [];
  });
  readonly usuario = computed(() => {
    const claims = this.sesion() ? decodeJwtPayload(this.sesion()!.accessToken) : null;
    return claims
      ? { sub: claims.sub, nombre: claims.name || claims.preferred_username || 'Usuario' }
      : null;
  });
  constructor() {
    this.programarCaducidad();
  }
  tieneRol(...roles: Rol[]): boolean {
    return roles.some((role) => this.roles().includes(role));
  }
  accessToken(): string | null {
    return this.sesion()?.accessToken ?? null;
  }
  async asegurarSesion(): Promise<boolean> {
    this.reloj.set(Date.now());
    return this.autenticado() || (await this.refrescar());
  }
  async iniciarLogin(destino = location.pathname + location.search, registro = false, transporte?: (url: string) => void): Promise<void> {
    if (this.loginEnCurso) return;
    this.loginEnCurso = true;
    this.error.set('');
    try {
      const verifier = generarCodeVerifier();
      const state = (transporte ? 'popup.' : '') + generarState();
      const challenge = await generarCodeChallenge(verifier);
      sessionStorage.setItem(AUTH_KEYS.verifier, verifier);
      sessionStorage.setItem(AUTH_KEYS.state, state);
      sessionStorage.setItem(AUTH_KEYS.destino, this.destinoSeguro(destino));
      const params = new URLSearchParams({
        client_id: environment.keycloak.clientId,
        response_type: 'code',
        redirect_uri: environment.keycloak.redirectUri,
        scope: 'openid profile email',
        state,
        code_challenge: challenge,
        code_challenge_method: 'S256',
      });
      if (registro && !transporte) sessionStorage.setItem('la_brava_registro', '1');
      (transporte || this.redirigir)(`${environment.keycloak.issuer}/protocol/openid-connect/${registro ? 'registrations' : 'auth'}?${params}`);
    } catch {
      this.loginEnCurso = false;
      this.error.set('No se pudo iniciar sesión. Intenta nuevamente.');
    }
  }
  iniciarLoginVentana(registro = false, signal?: AbortSignal): Promise<boolean> {
    let ventana: Window | null = null;
    return this.iniciarLoginCanal(registro, url => ventana = window.open(url, 'la-brava-oidc-' + generarState(), 'popup,width=520,height=740'), () => ventana?.close(), () => !!ventana?.closed, signal);
  }
  iniciarLoginMarco(marco: HTMLIFrameElement, registro = false, signal?: AbortSignal): Promise<boolean> {
    if (new URL(environment.keycloak.issuer).origin !== location.origin) { this.error.set('Utiliza la ventana segura para este acceso.'); return Promise.resolve(false); }
    return this.iniciarLoginCanal(registro, url => { marco.src = url; return marco.contentWindow; }, () => { if (marco.isConnected) marco.src = 'about:blank'; }, () => !marco.isConnected, signal);
  }
  private iniciarLoginCanal(registro: boolean, abrir: (url: string) => Window | null, cerrar: () => void, cerrado: () => boolean, signal?: AbortSignal): Promise<boolean> {
    if (this.loginEnCurso || signal?.aborted) return Promise.resolve(false);
    this.error.set(''); sessionStorage.removeItem('la_brava_registro');
    return new Promise(resolve => {
      let ventana: Window | null = null; let finalizado = false, intercambiando = false; const solicitud = new AbortController();
      const terminar = (ok: boolean, mensaje = '') => { if (finalizado) return; finalizado = true; if (!ok) solicitud.abort(); window.removeEventListener('message', recibir); signal?.removeEventListener('abort', cancelar); clearInterval(reloj); clearTimeout(limite); this.loginEnCurso = false; this.limpiarFlujo(); if (mensaje) this.error.set(mensaje); try { cerrar(); } catch { } resolve(ok); };
      const cancelar = () => terminar(false);
      const recibir = async (event: MessageEvent) => {
        if (!ventana || finalizado || intercambiando || event.origin !== location.origin || event.source !== ventana || event.data?.tipo !== 'la-brava-auth-callback') return;
        const { code, state, error } = event.data;
        if (typeof state !== 'string' || state !== sessionStorage.getItem(AUTH_KEYS.state)) return;
        if (error || typeof code !== 'string' || !code) { terminar(false, 'No se completó el acceso. Puedes intentarlo nuevamente.'); return; }
        intercambiando = true;
        try { await this.completarLogin(code, state, solicitud.signal); terminar(true); } catch { terminar(false, 'No se pudo validar el acceso. Intenta nuevamente.'); }
      };
      window.addEventListener('message', recibir); signal?.addEventListener('abort', cancelar, { once: true });
      const reloj = setInterval(() => { if (ventana && cerrado() && !intercambiando) terminar(false, 'Se cerró la ventana de acceso.'); }, 400);
      const limite = setTimeout(() => { if (!intercambiando) terminar(false, 'El acceso venció. Intenta nuevamente.'); }, 180000);
      void this.iniciarLogin(location.pathname + location.search, registro, url => { ventana = abrir(url); if (!ventana) this.error.set('Permite la ventana de acceso de La Brava y vuelve a intentarlo.'); }).then(() => { if (this.error()) terminar(false, this.error()); }).catch(() => terminar(false, 'No se pudo abrir el acceso.'));
    });
  }
  async completarLogin(code: string, state: string, signal?: AbortSignal): Promise<void> {
    const esperado = sessionStorage.getItem(AUTH_KEYS.state);
    const verifier = sessionStorage.getItem(AUTH_KEYS.verifier);
    sessionStorage.removeItem(AUTH_KEYS.state);
    sessionStorage.removeItem(AUTH_KEYS.verifier);
    if (!code || !state || !esperado || state !== esperado || !verifier) {
      this.descartarSesion();
      throw new Error('No se pudo validar el inicio de sesión. Inicia nuevamente.');
    }
    try {
      await this.intercambiar({
        grant_type: 'authorization_code',
        client_id: environment.keycloak.clientId,
        redirect_uri: environment.keycloak.redirectUri,
        code,
        code_verifier: verifier,
      }, signal);
    } catch {
      this.descartarSesion();
      throw new Error('No se pudo completar el inicio de sesión. Intenta nuevamente.');
    }
  }
  destinoPendiente(): string {
    const destino = this.destinoSeguro(sessionStorage.getItem(AUTH_KEYS.destino) ?? '/');
    sessionStorage.removeItem(AUTH_KEYS.destino);
    return destino;
  }
  refrescar(): Promise<boolean> {
    if (this.renovacion) return this.renovacion;
    const anterior = this.sesion();
    if (!anterior || anterior.refreshExpiraEn <= Date.now()) {
      this.descartarSesion();
      return Promise.resolve(false);
    }
    this.renovacion = (async () => {
      try {
        const datos = await this.solicitarTokens({
          grant_type: 'refresh_token',
          client_id: environment.keycloak.clientId,
          refresh_token: anterior.refreshToken,
        });
        if (this.sesion() !== anterior) return false;
        this.guardarSesion(datos);
        return true;
      } catch {
        if (this.sesion() === anterior) this.descartarSesion();
        return false;
      } finally {
        this.renovacion = null;
      }
    })();
    return this.renovacion;
  }
  cerrarSesion(): void {
    const idToken = this.sesion()?.idToken;
    this.descartarSesion();
    this.limpiarFlujo();
    const params = new URLSearchParams({
      client_id: environment.keycloak.clientId,
      post_logout_redirect_uri: environment.keycloak.postLogoutRedirectUri,
      ...(idToken ? { id_token_hint: idToken } : {}),
    });
    this.redirigir(`${environment.keycloak.issuer}/protocol/openid-connect/logout?${params}`);
  }
  descartarSesion(): void {
    this.sesion.set(null);
    sessionStorage.removeItem(AUTH_KEYS.sesion);
    clearTimeout(this.temporizador);
    this.reloj.set(Date.now());
  }
  limpiarFlujo(): void {
    for (const key of [AUTH_KEYS.state, AUTH_KEYS.verifier, AUTH_KEYS.destino])
      sessionStorage.removeItem(key);
  }
  private destinoSeguro(path: string): string {
    return path.startsWith('/') &&
      !path.startsWith('//') &&
      !path.includes('\\') &&
      !path.startsWith('/auth/')
      ? path
      : '/';
  }
  private async solicitarTokens(params: Record<string, string>, signal?: AbortSignal): Promise<TokenResponse> {
    const response = await fetch(`${environment.keycloak.issuer}/protocol/openid-connect/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams(params),
      signal,
    });
    if (!response.ok) throw new Error('No se pudo renovar la sesión.');
    const data: TokenResponse = await response.json();
    if (
      typeof data.access_token !== 'string' ||
      typeof data.refresh_token !== 'string' ||
      typeof data.id_token !== 'string' ||
      !Number.isFinite(data.expires_in) ||
      !Number.isFinite(data.refresh_expires_in)
    )
      throw new Error('Sesión inválida.');
    return data;
  }
  private async intercambiar(params: Record<string, string>, signal?: AbortSignal): Promise<void> {
    const datos = await this.solicitarTokens(params, signal); if (signal?.aborted) throw new DOMException('Acceso cancelado', 'AbortError'); this.guardarSesion(datos);
  }
  private guardarSesion(data: TokenResponse): void {
    const claims = decodeJwtPayload(data.access_token);
    if (!claims || claims.iss !== environment.keycloak.issuer || claims.exp * 1000 <= Date.now())
      throw new Error('Sesión inválida.');
    const s: Sesion = {
      accessToken: data.access_token,
      refreshToken: data.refresh_token,
      idToken: data.id_token,
      expiraEn: Math.min(claims.exp * 1000, Date.now() + data.expires_in * 1000),
      refreshExpiraEn: Date.now() + data.refresh_expires_in * 1000,
    };
    sessionStorage.setItem(AUTH_KEYS.sesion, JSON.stringify(s));
    this.sesion.set(s);
    this.programarCaducidad();
  }
  private restaurarSesion(): Sesion | null {
    try {
      const s: Sesion = JSON.parse(sessionStorage.getItem(AUTH_KEYS.sesion) ?? 'null');
      if (
        s &&
        typeof s.accessToken === 'string' &&
        typeof s.refreshToken === 'string' &&
        typeof s.idToken === 'string' &&
        Number.isFinite(s.expiraEn) &&
        s.refreshExpiraEn > Date.now()
      ) {
        const jwt = decodeJwtPayload(s.accessToken);
        if (
          jwt?.iss === environment.keycloak.issuer &&
          Number.isFinite(s.refreshExpiraEn) &&
          s.expiraEn <= jwt.exp * 1000
        )
          return s;
      }
    } catch {
      /* Copia inválida: se descarta sin mostrar su contenido. */
    }
    sessionStorage.removeItem(AUTH_KEYS.sesion);
    return null;
  }
  private programarCaducidad(): void {
    clearTimeout(this.temporizador);
    this.reloj.set(Date.now());
    const s = this.sesion();
    if (s)
      this.temporizador = setTimeout(
        () => this.reloj.set(Date.now()),
        Math.max(0, s.expiraEn - Date.now() + 5),
      );
  }
}
