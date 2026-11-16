function base64url(bytes: Uint8Array): string {
  return btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}
export function generarCodeVerifier(): string {
  return base64url(crypto.getRandomValues(new Uint8Array(32)));
}
export function generarState(): string {
  return base64url(crypto.getRandomValues(new Uint8Array(16)));
}
export async function generarCodeChallenge(verifier: string): Promise<string> {
  return base64url(
    new Uint8Array(await crypto.subtle.digest('SHA-256', new TextEncoder().encode(verifier))),
  );
}
