#!/usr/bin/env python3
"""Backup lógico consistente. Solo recursos POLLERIA indicados explícitamente."""
import argparse,hashlib,json,os,subprocess
from datetime import datetime,timezone
from pathlib import Path

def run(args,**kw):return subprocess.run(args,check=True,capture_output=True,**kw)
def main():
    p=argparse.ArgumentParser();p.add_argument('--oracle',required=True);p.add_argument('--postgres',required=True);p.add_argument('--output',required=True);a=p.parse_args()
    assert a.oracle.startswith('polleria-') and a.postgres.startswith('polleria-'),'Solo POLLERIA'
    out=Path(a.output).resolve();repo=Path(__file__).resolve().parents[2];assert not out.is_relative_to(repo),'Backup debe estar fuera del repositorio'
    out.mkdir(parents=True,exist_ok=True,mode=0o700);os.chmod(out,0o700);stamp=datetime.now(timezone.utc).strftime('%Y%m%dT%H%M%SZ');name='polleria-'+stamp
    sql="ALTER SESSION SET CONTAINER=FREEPDB1;\nSET HEADING OFF FEEDBACK OFF PAGESIZE 0\nSELECT DIRECTORY_PATH FROM DBA_DIRECTORIES WHERE DIRECTORY_NAME='DATA_PUMP_DIR';\nEXIT\n"
    raw=run(['docker','exec','-i',a.oracle,'sqlplus','-s','/ as sysdba'],input=sql.encode()).stdout.decode()
    directory=next(x.strip() for x in raw.splitlines() if x.strip().startswith('/'))
    # Autenticación local OS; ORACLE_PDB_SID selecciona el PDB, sin password.
    r=run(['docker','exec','-e','ORACLE_PDB_SID=FREEPDB1',a.oracle,'expdp','"/ as sysdba"','schemas=POLLERIA','directory=DATA_PUMP_DIR','dumpfile='+name+'.dmp','logfile='+name+'.log','flashback_time=systimestamp'])
    run(['docker','cp',a.oracle+':'+directory+'/'+name+'.dmp',str(out/(name+'.dmp'))])
    pg=run(['docker','exec',a.postgres,'pg_dump','-U','polleria_keycloak','-d','polleria_keycloak','--format=custom','--no-owner','--no-acl']).stdout
    (out/(name+'.pgdump')).write_bytes(pg)
    for suffix in ['.dmp','.pgdump']:
        f=out/(name+suffix);f.chmod(0o600);assert f.stat().st_size>0
    toc=run(['docker','exec','-i',a.postgres,'pg_restore','--list'],input=pg).stdout
    assert b'TABLE DATA' in toc
    evidence={'fechaUTC':stamp,'oracle':a.oracle,'postgres':a.postgres,'expdpExit':r.returncode,'pgRestoreListOK':True,'destinoFueraRepo':True,'archivos':[{'nombre':f.name,'bytes':f.stat().st_size,'sha256':hashlib.sha256(f.read_bytes()).hexdigest(),'modo':oct(f.stat().st_mode&0o777)} for f in [out/(name+'.dmp'),out/(name+'.pgdump')]]}
    (out/(name+'.manifest.json')).write_text(json.dumps(evidence,indent=2)+'\n');(out/(name+'.manifest.json')).chmod(0o600)
    print(json.dumps(evidence,indent=2))
if __name__=='__main__':
    try:main()
    except subprocess.CalledProcessError as exc:raise SystemExit('Backup falló; no se publica salida potencialmente sensible. Código '+str(exc.returncode))
