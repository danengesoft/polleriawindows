#!/usr/bin/env python3
"""Smoke HTTP público sin credenciales; health interno se consulta por Compose."""
import argparse,json
from urllib.request import Request,urlopen
from urllib.error import HTTPError
p=argparse.ArgumentParser();p.add_argument('--url',required=True);a=p.parse_args();base=a.url.rstrip('/');result=[]
for path,expected in [('/',200),('/auth/callback',200),('/catalogo',200),('/carrito',200),('/mis-pedidos',200),('/perfil',200),('/interno/productos',200),('/interno/pedidos',200),('/interno/reportes',200),('/api/v1/productos',200),('/api/v1/clientes',401),('/actuator/health',404),('/v3/api-docs',404),('/auth/admin/',404),('/auth/realms/polleria/.well-known/openid-configuration',200)]:
 try:r=urlopen(Request(base+path,headers={'X-Trace-ID':'fase18-proxy'}),timeout=15)
 except HTTPError as e:r=e
 with r:
  raw=r.read();assert r.status==expected,(path,r.status)
  row={'path':path,'http':r.status}
  if path.startswith('/api/'):
   assert r.headers.get('X-Trace-ID')=='fase18-proxy';row['traceIdOK']=True
  if path=='/auth/callback':
   assert b'<app-root' in raw, 'El retorno de acceso debe cargar Angular'
  if path=='/':
   row['headers']={k:r.headers[k] for k in ['X-Content-Type-Options','Referrer-Policy','X-Frame-Options','Content-Security-Policy']};assert all(row['headers'].values())
  if path.endswith('openid-configuration'):
   d=json.loads(raw);assert d['issuer']==base+'/auth/realms/polleria';assert all('keycloak:8080' not in str(v) for v in d.values());row['issuer']=d['issuer']
  result.append(row)
print(json.dumps({'completado':True,'requests':result},indent=2))
