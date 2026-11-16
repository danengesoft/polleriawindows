// Configuración local de esta fase. Producción tendrá su reemplazo cuando exista un destino real.
export const environment = {
  apiBaseUrl: 'http://localhost:8080',
  keycloak: {
    issuer: 'http://localhost:38080/realms/polleria',
    clientId: 'polleria-frontend',
    redirectUri: 'http://localhost:4200/auth/callback',
    postLogoutRedirectUri: 'http://localhost:4200/',
  },
};
