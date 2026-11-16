// Un solo origen público: Nginx publica SPA, /api y /auth.
const origin = window.location.origin;
export const environment = {
  apiBaseUrl: '',
  keycloak: {
    issuer: `${origin}/auth/realms/polleria`,
    clientId: 'polleria-frontend',
    redirectUri: `${origin}/auth/callback`,
    postLogoutRedirectUri: `${origin}/`,
  },
};
