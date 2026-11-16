#!/usr/bin/env python3
"""Validación y render de configuración pública; nunca imprime secretos."""
import argparse,json,os,re,socket,stat,subprocess
from pathlib import Path
from urllib.parse import urlparse
ROOT=Path(__file__).resolve().parents[2]
def load(path):
    return dict(line.split('=',1) for line in path.read_text().splitlines() if line.strip() and not line.lstrip().startswith('#') and '=' in line)
def main():
    p=argparse.ArgumentParser();p.add_argument('--env-file',required=True);p.add_argument('--local-test',action='store_true');a=p.parse_args();f=Path(a.env_file).resolve();e=load(f)
    assert stat.S_IMODE(f.stat().st_mode)==0o600,'El archivo de entorno requiere0600'
    assert subprocess.run(['git','check-ignore','-q',str(f)],cwd=ROOT).returncode==0,'Entorno no ignorado'
    assert subprocess.run(['git','ls-files','--error-unmatch',str(f)],cwd=ROOT,stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL).returncode!=0,'Entorno versionado'
    for k in ['COMPOSE_PROJECT_NAME','IMAGE_TAG','APP_DOMAIN','PUBLIC_APP_URL','ORACLE_APP_USER','ORACLE_PASSWORD','ORACLE_APP_PASSWORD','KEYCLOAK_DB_PASSWORD','KEYCLOAK_ADMIN_USERNAME','KEYCLOAK_ADMIN_PASSWORD']:
        assert e.get(k) and not re.search(r'REPLACE|placeholder|example|changeme',e[k],re.I),'Variable ausente/placeholder: '+k
    for k in ['ORACLE_PASSWORD','ORACLE_APP_PASSWORD','KEYCLOAK_DB_PASSWORD','KEYCLOAK_ADMIN_PASSWORD']:
        assert len(e[k])>=24 and e[k].lower() not in ['admin','password','123456'],'Secreto insuficiente: '+k
    assert len(set(e[k] for k in ['ORACLE_PASSWORD','ORACLE_APP_PASSWORD','KEYCLOAK_DB_PASSWORD','KEYCLOAK_ADMIN_PASSWORD']))==4,'Secretos deben ser diferentes'
    assert e['ORACLE_APP_USER']=='POLLERIA','Scripts requieren schema POLLERIA'
    assert e['KEYCLOAK_ADMIN_USERNAME'].lower()!='admin','Elegir usuario bootstrap específico'
    assert re.fullmatch(r'[A-Za-z0-9.-]+',e['APP_DOMAIN']),'APP_DOMAIN no válido'
    url=urlparse(e['PUBLIC_APP_URL']);assert not url.path and not url.query and not url.fragment and url.hostname,'URL debe ser origen sin slash final'
    assert e.get('BIND_ADDRESS','127.0.0.1')=='127.0.0.1','Barrera: binding público requiere revisión y autorización posterior'
    if a.local_test:
        assert e['COMPOSE_PROJECT_NAME']=='polleria-prodtest' and e['PUBLIC_APP_URL']=='http://127.0.0.1:18080'
    else:
        assert url.scheme=='https' and url.netloc==e['APP_DOMAIN'] and url.hostname not in ['localhost','127.0.0.1'],'Producción requiere dominio HTTPS aprobado'
        assert (ROOT/'production/certs/fullchain.pem').is_file() and (ROOT/'production/certs/privkey.pem').is_file(),'Faltan certificados locales aprobados'
        assert stat.S_IMODE((ROOT/'production/certs/privkey.pem').stat().st_mode)&0o077==0,'Clave privada requiere0600'
    ports=[18080] if a.local_test else [int(e.get('HTTP_PORT','80')),int(e.get('HTTPS_PORT','443'))]
    for port in ports:
        with socket.socket() as sock:
            sock.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
            sock.bind(('127.0.0.1',port))
    subprocess.run(['docker','info','--format','{{.ServerVersion}}'],check=True,stdout=subprocess.DEVNULL)
    realm=json.loads((ROOT/'polleria-backend/keycloak/polleria-realm.json').read_text());c=realm['clients'][0]
    c['redirectUris']=[e['PUBLIC_APP_URL']+'/auth/callback'];c['webOrigins']=[e['PUBLIC_APP_URL']];c['attributes']['post.logout.redirect.uris']=e['PUBLIC_APP_URL']+'/'
    realm['sslRequired']='external' if a.local_test else 'all'
    dest=ROOT/'production/generated'/e['COMPOSE_PROJECT_NAME'];dest.mkdir(parents=True,exist_ok=True);(dest/'polleria-realm.json').write_text(json.dumps(realm,indent=2)+'\n')
    command=['docker','compose','--env-file',str(f),'-f',str(ROOT/'compose.prod.yml')]
    if a.local_test:command+=['-f',str(ROOT/'compose.prodtest.yml')]
    cfg=json.loads(subprocess.check_output(command+['config','--format','json']))
    for name,svc in cfg['services'].items():
        assert name=='frontend' or not svc.get('ports'), 'Puerto publicado fuera de Nginx'
    print('Preflight OK: entorno privado, sin placeholders, puertos libres, Compose válido, realm sin usuarios generado.')
if __name__=='__main__':
    try:main()
    except (AssertionError,OSError,subprocess.CalledProcessError) as exc:raise SystemExit('Preflight rechazado: '+str(exc))
