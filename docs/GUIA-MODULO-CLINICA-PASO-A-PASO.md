# Guía paso a paso: construir el módulo clínica desde cero

Esta guía explica cómo construir **todo** el módulo `clinica` del examen final práctico v7, con base
de datos, backend y SPA, dentro del proyecto de la pollería. Sigue el orden del examen:

| Etapa | Qué construyes | Pregunta del examen | Archivos |
|---|---|---|---:|
| 0 | Preparar el proyecto sin el módulo y la base de datos | Pregunta 1 (BD, 2 pts) | 3 |
| 1 | CRUD de **Clínicas** con validaciones | Pregunta 1 (5 pts) | 18 |
| 2 | Listado de **Especialidades** | Pregunta 2 (1 pt) | 12 |
| 3 | **Consultorios** con sus especialidades (cabecera con detalles) | Pregunta 2 (12 pts) | 19 |

**Cada paso trae el archivo completo.** No hay que buscar dónde va cada bloque: si el archivo es
**📄 nuevo**, lo creas y pegas el código; si es **✏️ reemplazar todo**, lo abres, borras todo y
pegas.

**Al terminar cada etapa, el proyecto compila y esa parte ya funciona en la página.** Algunos
archivos (`clinica.model.ts`, `clinica-service.ts`, `app.routes.ts`, el menú y
`ClinicaServiceImpl.java`) aparecen en varias etapas: cada vez traen la versión más completa.
Siempre reemplaza todo.

Todo el código se probó así: se armó el proyecto sin el módulo, se siguió esta guía etapa por
etapa, se compiló y se probó cada etapa en el navegador. Al final, el proyecto quedó **idéntico**
al que está en GitHub.

Para entender el porqué de cada parte, consulta también `docs\GUIA-MODULO-CLINICA.md`.

---

## Etapa 0. Preparar la práctica

Tu proyecto **ya tiene** el módulo clínica. Para practicar desde cero sin perder nada, trabajas en
una **rama de práctica**: un espacio aparte donde quitas el módulo y lo vuelves a construir. Tu
`main` no se toca.

### 0.1. Crear la rama y quitar el módulo

En PowerShell, desde `C:\polleriawindows`:

```bash
git checkout -b practica-clinica
```

```bash
Remove-Item -Recurse -Force polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica
```

```bash
Remove-Item -Recurse -Force polleria-frontend\src\app\features\clinica
```

Luego deja estos 3 archivos **sin nada del módulo clínica**, con los pasos 0.2 a 0.4.

### 0.2. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\SecurityConfig.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código. Es la seguridad **sin** la regla del módulo clínica.

```java
package pe.edu.upeu.polleria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
public class SecurityConfig {
        private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "CAJERO",
                        "CLIENTE", "OPERACIONES", "MESERO");

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                // The default principal name is the validated JWT subject (String).
                converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        Map<String, Object> access = jwt.getClaimAsMap("realm_access");
                        Object roles = access == null ? null : access.get("roles");
                        if (!(roles instanceof Collection<?> values))
                                return List.<GrantedAuthority>of();
                        return values.stream().filter(String.class::isInstance).map(String.class::cast)
                                        .filter(ROLES::contains)
                                        .filter(role -> role.equals("CLIENTE") || (Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")) && jwt.getClaimAsString("email") != null && !jwt.getClaimAsString("email").isBlank()))
                                        .distinct()
                                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                                        .toList();
                });
                return converter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter,
                        SecurityErrorHandler errors) throws Exception {
                http.cors(cors -> {
                })
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(
                                                logout -> logout.disable())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(errors).accessDeniedHandler(
                                                errors))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET,"/api/v1/promociones").permitAll()
                                                .requestMatchers("/api/v1/promociones", "/api/v1/promociones/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/destacados").permitAll()
                                                .requestMatchers("/api/v1/destacados", "/api/v1/destacados/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/ubicacion").permitAll()
                                                .requestMatchers("/api/v1/ubicacion", "/api/v1/ubicacion/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/banners").permitAll()
                                                .requestMatchers("/api/v1/banners", "/api/v1/banners/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.POST,"/api/v1/productos/imagenes").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/productos/imagenes/*").permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/web/sesion","/api/v1/web/sesion/revocar","/api/v1/web/pedidos","/api/v1/web/pedidos/*/clave","/api/v1/web/pedidos/*/recuperar","/api/v1/web/pedidos/*/revocar","/api/v1/web/pedidos/*/pago-prueba").permitAll()
                                                .requestMatchers(HttpMethod.GET,"/api/v1/web/mesas","/api/v1/web/mesas/eventos","/api/v1/web/pedidos/*").permitAll()
                                                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC,jakarta.servlet.DispatcherType.ERROR).permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/pedidos/*/pago-prueba").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/eventos").hasAnyRole("ADMINISTRADOR","CAJERO","OPERACIONES","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesas").hasAnyRole("ADMINISTRADOR","CAJERO","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/mesas/*/ocupar","/api/v1/mesas/*/liberar").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/operaciones/pedidos").hasAnyRole("OPERACIONES","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesero/pedidos").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/caja/me").hasAnyRole("CAJERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/estado").hasAnyRole("OPERACIONES","MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/actuator/health",
                                                                "/actuator/health/**")
                                                .permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/categorias",
                                                                "/api/v1/categorias/*",
                                                                "/api/v1/productos", "/api/v1/productos/*")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/categorias",
                                                                "/api/v1/productos")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios", "/api/v1/pedidos/mios/*")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/me")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/mios/*/cancelar")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes",
                                                                "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/v1/delivery/**").denyAll()
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/confirmar-pago").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                // Reports must precede general order matchers.
                                                .requestMatchers("/api/v1/pedidos/reportes/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos", "/api/v1/pedidos/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .anyRequest().denyAll())
                                .oauth2ResourceServer(oauth -> oauth.authenticationEntryPoint(errors)
                                                .accessDeniedHandler(errors)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
                return http.build();
        }
}
```

### 0.3. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\app.routes.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código. Son las rutas **sin** las del módulo clínica.

```ts
import { authGuard, authChildGuard } from './core/guards/auth-guard';
import { rolGuard,inicioInternoGuard,publicoGuard } from './core/guards/rol-guard';
import { INTERNOS, OPERATIVOS } from './core/auth/auth-service';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'inicio',canActivate:[publicoGuard],title:'Pollería Neyos | Pollos y Parrillas',loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'destacados',canActivate:[publicoGuard],title:'Destacados de la carta | Polleria Neyos',data:{seccion:'destacados'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'promociones',canActivate:[publicoGuard],title:'Promociones | Polleria Neyos',data:{seccion:'promociones'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'acceso',canActivate:[publicoGuard],loadComponent:()=>import('./features/publico/acceso').then(m=>m.Acceso)},
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: 'sin-permiso',
    loadComponent: () => import('./core/auth/sin-permiso').then((m) => m.SinPermiso),
  },
  {
    path: 'interno',
    canActivate: [authGuard, rolGuard(...INTERNOS)],
    canActivateChild: [authChildGuard, rolGuard(...INTERNOS)],
    loadComponent: () => import('./core/layout/interno/interno').then((m) => m.Interno),
    children: [
      {path:'ubicacion',redirectTo:'portada',pathMatch:'full'},
      {path:'portada',canActivate:[rolGuard('ADMINISTRADOR')],title:'Local y página de inicio | Pollería Neyos',loadComponent:()=>import('./features/contenido/banners').then(m=>m.Banners)},
      {path:'caja',canActivate:[rolGuard('CAJERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Caja)},
      {path:'operaciones',canActivate:[rolGuard('OPERACIONES','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Operaciones)},
      {path:'mesero',canActivate:[rolGuard('MESERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Mesero)},
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMINISTRADOR')],
        title: 'Reportes | Pollería Neyos',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        pathMatch: 'full',
        canActivate:[inicioInternoGuard],
        title: 'Portal interno | Pollería Neyos',
        loadComponent: () => import('./core/inicio/inicio-interno').then((m) => m.InicioInterno),
      },
      {
        path: 'categorias',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'categorias/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'categorias/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'productos',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'productos/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'productos/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'clientes',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'pedidos',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Pedidos | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-list').then((m) => m.PedidoList),
      },
      {
        path: 'pedidos/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Nuevo pedido | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-form').then((m) => m.PedidoForm),
      },
      {
        path: 'pedidos/:id',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Detalle del pedido | Pollería Neyos',
        loadComponent: () =>
          import('./features/pedidos/pedido-detalle').then((m) => m.PedidoDetalle),
      },

    ],
  },
  {
    path: '',
    canActivateChild: [publicoGuard],
    loadComponent: () => import('./core/layout/publico/publico').then((m) => m.Publico),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/publico/catalogo-publico').then((m) => m.CatalogoPublico),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/publico/carrito').then((m) => m.Carrito),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/perfil').then((m) => m.Perfil),
      },
      {
        path: 'checkout',
        loadComponent: () => import('./features/publico/checkout').then((m) => m.Checkout),
      },
      {path:'seguimiento/:id',data:{web:true},loadComponent:()=>import('./features/publico/mi-pedido').then(m=>m.MiPedido)},
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'mis-pedidos/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mi-pedido').then((m) => m.MiPedido),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

### 0.4. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\core\layout\interno\interno.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código. Es el menú **sin** la sección Clínica.

```html
<a class="skip-link" href="#contenido">Saltar al contenido</a>
<header class="site-header">
  <div class="container header-row">
    <a class="brand" routerLink="/interno">Polleria Neyos · Restaurant Pollos y Parrillas</a>
    <div class="session-row">
      <button type="button" class="secondary portal-toggle" #portalButton [attr.aria-expanded]="menuAbierto()"
        aria-controls="portal-menu" (click)="menuAbierto.set(!menuAbierto())">
        Menú
      </button>
      <app-navigation [interno]="true" />
    </div>
  </div>
</header>
<div class="container internal-body">
  <aside (keydown.escape)="cerrarMenu(portalButton)">

    <p class="eyebrow">Portal interno</p>
    <nav id="portal-menu" aria-label="Navegación del portal" [class.abierto]="menuAbierto()"
      (click)="menuAbierto.set(false)">
      @if(auth.tieneRol('CAJERO') && !auth.tieneRol('ADMINISTRADOR')){<a routerLink="/interno/caja"
        routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Mi caja</a><a routerLink="/interno/caja"
        [queryParams]="{vista:'pendientes'}" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos pendientes</a><a
        routerLink="/interno/caja" [queryParams]="{vista:'entregados'}" routerLinkActive="active"
        [routerLinkActiveOptions]="{exact:true}" ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos
        entregados</a>}
      @if(auth.tieneRol('OPERACIONES','ADMINISTRADOR')){<a routerLink="/interno/operaciones"
        routerLinkActive="active"><app-icono-portal nombre="cocina" />Operaciones</a>}
      @if(auth.tieneRol('MESERO','ADMINISTRADOR')){<a routerLink="/interno/mesero"
        routerLinkActive="active"><app-icono-portal nombre="mesa" />Mesero</a>}
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Gestión</span>
      <a routerLink="/interno/portada" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Local y página de inicio</a>
      <a routerLink="/interno/categorias" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Categorías</a>
      <a routerLink="/interno/productos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Productos</a>
      <a routerLink="/interno/clientes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clientes</a>
      <a routerLink="/interno/pedidos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="pedido" />Pedidos</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <a routerLink="/interno/reportes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="reporte" />Reportes</a>
      }

    </nav>
  </aside>
  <main id="contenido" tabindex="-1"><router-outlet /></main>
</div>
```

### 0.5. Comprobar que compila sin el módulo

```bash
cd C:\polleriawindows\polleria-backend
```

```bash
.\mvnw.cmd -q -DskipTests package
```

Si no muestra nada, está listo para empezar.

> **Consejo:** abre `.\desarrollo.bat` en otra ventana de PowerShell (con el sistema levantado
> con `iniciar.bat`). Así ves cada cambio en la página sin compilar las imágenes de Docker.

### 0.6. Base de datos (pregunta 1 · 2 pts)

`iniciar.bat` ya creó el esquema `EXA_CLINICA` con sus 4 tablas y los datos de ejemplo (script
`polleria-backend\scripts\02-clinica.sql`). Para **listar las tablas**, como pide el examen:

```powershell
$DB = 'polleria-prodtest-postgres-1'
@'
SELECT table_name FROM information_schema.tables WHERE table_schema = 'exa_clinica' ORDER BY 1;
'@ | docker exec -i $DB psql -U polleria -d polleria
```

Debe mostrar `clinicas`, `consultorio_especialidad`, `consultorios` y `especialidades`.

**Para practicar también la creación** (como en el examen), borra el esquema y vuelve a crearlo
con el script:

```powershell
$DB = 'polleria-prodtest-postgres-1'
'DROP SCHEMA IF EXISTS EXA_CLINICA CASCADE;' | docker exec -i $DB psql -U polleria -d polleria
Get-Content polleria-backend\scripts\02-clinica.sql -Raw | docker exec -i $DB psql -U polleria -d polleria -v ON_ERROR_STOP=1
```

> Borrar el esquema elimina también lo que hayas creado en clínicas y consultorios; se vuelve a
> los datos de ejemplo.

---

## Etapa 1. Clínicas: CRUD con validaciones (pregunta 1 · 5 pts)

**Archivos de esta etapa** (en este orden):

| Archivo | Acción |
|---|---|
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\package-info.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\entity\Clinica.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\dto\ClinicaRequest.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\dto\ClinicaResponse.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\mapper\ClinicaMapper.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\repository\ClinicaRepository.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaService.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaServiceImpl.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\controller\ClinicaController.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\SecurityConfig.java` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\clinica.model.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica-service.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica-list.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica-list.html` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica-form.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica-form.html` | 📄 Nuevo |
| `polleria-frontend\src\app\app.routes.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\core\layout\interno\interno.html` | ✏️ Reemplazar todo |

### 1.1. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\package-info.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Marca el paquete `clinica` como un **módulo** nuevo de Spring Modulith (S01).

```java
/**
 * Módulo clinica (examen final práctico v7): clínicas, especialidades y
 * consultorios con sus especialidades (cabecera con detalles). Tablas en el
 * esquema EXA_CLINICA.
 */
package pe.edu.upeu.polleria.clinica;
```

### 1.2. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\entity\Clinica.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La entidad JPA de la tabla `EXA_CLINICA.CLINICAS`. `@JdbcTypeCode` le dice a Hibernate que `RUC` es `CHAR(11)` y que `ACTIVO` es `SMALLINT` 0/1. Sin eso el backend **no arranca**, porque revisa que la entidad coincida con la tabla (`ddl-auto: validate`).

```java
package pe.edu.upeu.polleria.clinica.clinica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.NumericBooleanConverter;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "CLINICAS", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // CHAR(11) en la base: el RUC siempre tiene 11 caracteres.
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "RUC", nullable = false, length = 11, unique = true)
    private String ruc;

    @Column(name = "RAZON_SOCIAL", nullable = false, length = 150)
    private String razonSocial;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    // La columna es SMALLINT 0/1; en Java se trabaja como Boolean.
    @Convert(converter = NumericBooleanConverter.class)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;
}
```

### 1.3. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\dto\ClinicaRequest.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lo que llega al crear o editar. Aquí están las **validaciones del examen**: `@Pattern("\\d{11}")` (RUC de 11 dígitos) y `@NotBlank` (razón social obligatoria).

```java
package pe.edu.upeu.polleria.clinica.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicaRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social admite hasta 150 caracteres")
    private String razonSocial;

    @Size(max = 20, message = "El teléfono admite hasta 20 caracteres")
    private String telefono;

    @NotNull
    private Boolean activo;
}
```

### 1.4. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\dto\ClinicaResponse.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lo que el backend devuelve. Así no se expone la entidad JPA.

```java
package pe.edu.upeu.polleria.clinica.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicaResponse {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String telefono;
    private Boolean activo;
}
```

### 1.5. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\mapper\ClinicaMapper.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

MapStruct convierte entre DTO y entidad. `id` se ignora porque lo genera la base de datos.

```java
package pe.edu.upeu.polleria.clinica.clinica.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ClinicaMapper {

    @Mapping(target = "id", ignore = true)
    Clinica toEntity(ClinicaRequest request);

    ClinicaResponse toResponse(Clinica clinica);

    @Mapping(target = "id", ignore = true)
    void actualizar(ClinicaRequest request, @MappingTarget Clinica clinica);
}
```

### 1.6. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\repository\ClinicaRepository.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Spring Data JPA. `existsByRuc` y `existsByRucAndIdNot` sirven para que el RUC no se repita, al crear y al editar.

```java
package pe.edu.upeu.polleria.clinica.clinica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;

public interface ClinicaRepository extends JpaRepository<Clinica, Long> {

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, Long id);
}
```

### 1.7. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaService.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La interfaz con los 5 métodos del CRUD.

```java
package pe.edu.upeu.polleria.clinica.clinica.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;

public interface ClinicaService {
    List<ClinicaResponse> listar();

    ClinicaResponse obtener(Long id);

    ClinicaResponse crear(ClinicaRequest request);

    ClinicaResponse actualizar(Long id, ClinicaRequest request);

    void eliminar(Long id);
}
```

### 1.8. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaServiceImpl.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La lógica: RUC repetido responde **409** y un id inexistente responde **404**.

> En la etapa 3 este archivo se reemplaza por una versión que **no deja borrar una clínica que tiene consultorios**. Ahora todavía no existen los consultorios.

```java
package pe.edu.upeu.polleria.clinica.clinica.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.clinica.mapper.ClinicaMapper;
import pe.edu.upeu.polleria.clinica.clinica.repository.ClinicaRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ClinicaServiceImpl implements ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final ClinicaMapper clinicaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClinicaResponse> listar() {
        return clinicaRepository.findAll(Sort.by("razonSocial")).stream().map(clinicaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicaResponse obtener(Long id) {
        return clinicaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ClinicaResponse crear(ClinicaRequest request) {
        normalizar(request);
        if (clinicaRepository.existsByRuc(request.getRuc()))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        Clinica clinica = clinicaMapper.toEntity(request);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public ClinicaResponse actualizar(Long id, ClinicaRequest request) {
        Clinica clinica = buscarOFallar(id);
        normalizar(request);
        if (clinicaRepository.existsByRucAndIdNot(request.getRuc(), id))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        clinicaMapper.actualizar(request, clinica);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Clinica clinica = buscarOFallar(id);
        clinicaRepository.delete(clinica);
    }

    private Clinica buscarOFallar(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica no encontrada: " + id));
    }

    private void normalizar(ClinicaRequest request) {
        request.setRuc(request.getRuc().strip());
        request.setRazonSocial(request.getRazonSocial().strip());
        if (request.getTelefono() != null) {
            String telefono = request.getTelefono().strip();
            request.setTelefono(telefono.isEmpty() ? null : telefono);
        }
    }
}
```

### 1.9. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\controller\ClinicaController.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Los 5 endpoints REST en `/api/v1/clinica/clinicas`: GET lista, GET por id, POST, PUT y DELETE.

```java
package pe.edu.upeu.polleria.clinica.clinica.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.service.ClinicaService;

@RestController
@RequestMapping("/api/v1/clinica/clinicas")
@RequiredArgsConstructor
public class ClinicaController {

    private final ClinicaService service;

    @GetMapping
    public ResponseEntity<List<ClinicaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ClinicaResponse> crear(@Valid @RequestBody ClinicaRequest request) {
        ClinicaResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clinica/clinicas/" + response.getId())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ClinicaRequest request) {
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 1.10. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\SecurityConfig.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

La seguridad del backend. Se agregan **2 líneas** (están casi al final, con el comentario `// Módulo clinica`) que dan acceso a `/api/v1/clinica/**` solo al ADMINISTRADOR. Van **antes** de `.anyRequest().denyAll()`; si no, todo el módulo responde 403.

```java
package pe.edu.upeu.polleria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
public class SecurityConfig {
        private static final Set<String> ROLES = Set.of("ADMINISTRADOR", "CAJERO",
                        "CLIENTE", "OPERACIONES", "MESERO");

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                // The default principal name is the validated JWT subject (String).
                converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                        Map<String, Object> access = jwt.getClaimAsMap("realm_access");
                        Object roles = access == null ? null : access.get("roles");
                        if (!(roles instanceof Collection<?> values))
                                return List.<GrantedAuthority>of();
                        return values.stream().filter(String.class::isInstance).map(String.class::cast)
                                        .filter(ROLES::contains)
                                        .filter(role -> role.equals("CLIENTE") || (Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")) && jwt.getClaimAsString("email") != null && !jwt.getClaimAsString("email").isBlank()))
                                        .distinct()
                                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                                        .toList();
                });
                return converter;
        }

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter converter,
                        SecurityErrorHandler errors) throws Exception {
                http.cors(cors -> {
                })
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))
                                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                                .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(
                                                logout -> logout.disable())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(errors).accessDeniedHandler(
                                                errors))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.GET,"/api/v1/promociones").permitAll()
                                                .requestMatchers("/api/v1/promociones", "/api/v1/promociones/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/destacados").permitAll()
                                                .requestMatchers("/api/v1/destacados", "/api/v1/destacados/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/ubicacion").permitAll()
                                                .requestMatchers("/api/v1/ubicacion", "/api/v1/ubicacion/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/banners").permitAll()
                                                .requestMatchers("/api/v1/banners", "/api/v1/banners/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.POST,"/api/v1/productos/imagenes").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/productos/imagenes/*").permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/web/sesion","/api/v1/web/sesion/revocar","/api/v1/web/pedidos","/api/v1/web/pedidos/*/clave","/api/v1/web/pedidos/*/recuperar","/api/v1/web/pedidos/*/revocar","/api/v1/web/pedidos/*/pago-prueba").permitAll()
                                                .requestMatchers(HttpMethod.GET,"/api/v1/web/mesas","/api/v1/web/mesas/eventos","/api/v1/web/pedidos/*").permitAll()
                                                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ASYNC,jakarta.servlet.DispatcherType.ERROR).permitAll()
                                                .requestMatchers(HttpMethod.POST,"/api/v1/pedidos/*/pago-prueba").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/pedidos").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/eventos").hasAnyRole("ADMINISTRADOR","CAJERO","OPERACIONES","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesas").hasAnyRole("ADMINISTRADOR","CAJERO","MESERO","CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/mesas/*/ocupar","/api/v1/mesas/*/liberar").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/operaciones/pedidos").hasAnyRole("OPERACIONES","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/mesero/pedidos").hasAnyRole("MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET,"/api/v1/caja/me").hasAnyRole("CAJERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/estado").hasAnyRole("OPERACIONES","MESERO","ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/actuator/health",
                                                                "/actuator/health/**")
                                                .permitAll()
                                                .requestMatchers("/actuator/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/categorias",
                                                                "/api/v1/categorias/*",
                                                                "/api/v1/productos", "/api/v1/productos/*")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/v1/categorias",
                                                                "/api/v1/productos")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias/*",
                                                                "/api/v1/productos/*")
                                                .hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios", "/api/v1/pedidos/mios/*")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes/me",
                                                                "/api/v1/pedidos/mios")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/me")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/mios/*/cancelar")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/clientes",
                                                                "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/clientes")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PUT, "/api/v1/clientes/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.DELETE, "/api/v1/clientes/*")
                                                .hasRole("ADMINISTRADOR")
                                                // Módulo clinica (examen v7): solo el administrador.
                                                .requestMatchers("/api/v1/clinica/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers("/api/v1/delivery/**").denyAll()
                                                .requestMatchers(HttpMethod.PATCH,"/api/v1/pedidos/*/confirmar-pago").hasAnyRole("ADMINISTRADOR","CAJERO")
                                                // Reports must precede general order matchers.
                                                .requestMatchers("/api/v1/pedidos/reportes/**").hasRole("ADMINISTRADOR")
                                                .requestMatchers(HttpMethod.GET, "/api/v1/pedidos", "/api/v1/pedidos/*")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/estado")
                                                .hasAnyRole("ADMINISTRADOR", "CAJERO")
                                                .anyRequest().denyAll())
                                .oauth2ResourceServer(oauth -> oauth.authenticationEntryPoint(errors)
                                                .accessDeniedHandler(errors)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));
                return http.build();
        }
}
```

### 1.11. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica.model.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Las interfaces TypeScript, con la misma forma que los DTO del backend. En las etapas 2 y 3 este archivo crece.

```ts
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
```

### 1.12. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica-service.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Llama al backend con `HttpClient`. El token de inicio de sesión lo agrega solo el interceptor que ya existe. En las etapas 2 y 3 este archivo crece.

```ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import {
  Clinica,
  ClinicaRequest,
} from './clinica.model';

// Un solo servicio para las tres pantallas del módulo: /api/v1/clinica/...
@Injectable({ providedIn: 'root' })
export class ClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly clinicas = '/api/v1/clinica/clinicas';

  listarClinicas() {
    return this.http.get<Clinica[]>(this.api.buildUrl(this.clinicas));
  }
  obtenerClinica(id: number) {
    return this.http.get<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
  crearClinica(data: ClinicaRequest) {
    return this.http.post<Clinica>(this.api.buildUrl(this.clinicas), data);
  }
  actualizarClinica(id: number, data: ClinicaRequest) {
    return this.http.put<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`), data);
  }
  eliminarClinica(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
}
```

### 1.13. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica-list.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La lógica del listado (Figura 2): carga los datos en una `signal` y elimina con confirmación.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Clinica } from './clinica.model';

@Component({
  selector: 'app-clinica-list',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './clinica-list.html',
})
export class ClinicaList {
  private readonly service = inject(ClinicaService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly datos = signal<Clinica[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');

  constructor() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.service
      .listarClinicas()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.datos.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }

  eliminar(item: Clinica) {
    this.dialog
      .open(ConfirmarEliminacion, { data: item.razonSocial, width: '26rem', maxWidth: 'calc(100vw - 2rem)' })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado !== true) return;
        this.error.set('');
        this.aviso.set('');
        this.service
          .eliminarClinica(item.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.aviso.set('Clínica eliminada.');
              this.cargar();
            },
            error: (err) => this.error.set(mensajeHttp(err)),
          });
      });
  }
}
```

### 1.14. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica-list.html`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La tabla de clínicas con **+ Nueva clínica**, **Editar** y **Eliminar**.

```html
<div class="page-heading">
  <h1>Clínicas</h1>
  <a matButton="filled" routerLink="/interno/clinica/clinicas/nuevo">+ Nueva clínica</a>
</div>

@if (aviso()) {
  <p role="status" class="notice">{{ aviso() }}</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}
@if (loading()) {
  <p role="status">Cargando clínicas…</p>
}

<div class="table-region" [attr.aria-busy]="loading()">
  <table class="data-table">
    <caption class="sr-only">Listado de clínicas</caption>
    <thead>
      <tr>
        <th scope="col">RUC</th>
        <th scope="col">Razón social</th>
        <th scope="col">Teléfono</th>
        <th scope="col">Activa</th>
        <th scope="col">Acciones</th>
      </tr>
    </thead>
    <tbody>
      @for (item of datos(); track item.id) {
        <tr>
          <td data-label="RUC">{{ item.ruc }}</td>
          <td data-label="Razón social">{{ item.razonSocial }}</td>
          <td data-label="Teléfono">{{ item.telefono || '—' }}</td>
          <td data-label="Activa">{{ item.activo ? 'Sí' : 'No' }}</td>
          <td data-label="Acciones" class="row-actions">
            <a matButton [routerLink]="['/interno/clinica/clinicas', item.id, 'editar']">Editar</a>
            <button matButton class="danger" type="button" (click)="eliminar(item)">Eliminar</button>
          </td>
        </tr>
      } @empty {
        @if (!loading() && !error()) {
          <tr><td class="empty" colspan="5">No hay clínicas registradas.</td></tr>
        }
      }
    </tbody>
  </table>
</div>
```

### 1.15. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica-form.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Un solo formulario para **crear** y **editar** (Figura 3). Tiene los mismos validadores que el backend. `markAllAsTouched()` hace que se vean los errores al pulsar **Guardar**.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { mensajeHttp } from '../../shared/http-error';
import { textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { ClinicaRequest } from './clinica.model';

// La misma pantalla sirve para crear (/nuevo) y editar (/:id/editar).
@Component({
  selector: 'app-clinica-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatCheckboxModule],
  templateUrl: './clinica-form.html',
})
export class ClinicaForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly id = signal<number | null>(null);
  readonly loading = signal(false);
  readonly guardando = signal(false);
  readonly error = signal('');

  // Validaciones del examen: RUC de 11 dígitos y razón social obligatoria.
  readonly form = this.fb.nonNullable.group({
    ruc: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
    razonSocial: ['', [textoObligatorio, Validators.maxLength(150)]],
    telefono: ['', [Validators.maxLength(20)]],
    activo: [true],
  });

  constructor() {
    const param = this.route.snapshot.paramMap.get('id');
    if (param === null) return;
    const id = Number(param);
    this.id.set(id);
    this.loading.set(true);
    this.service
      .obtenerClinica(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (c) => {
          this.form.patchValue({ ruc: c.ruc, razonSocial: c.razonSocial, telefono: c.telefono ?? '', activo: c.activo });
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }

  // Mensaje de error visible debajo de cada campo.
  mensaje(campo: 'ruc' | 'razonSocial' | 'telefono'): string {
    const control: AbstractControl = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (campo === 'ruc') {
      return control.errors['required'] ? 'El RUC es obligatorio' : 'El RUC debe tener 11 dígitos';
    }
    if (campo === 'razonSocial') {
      return control.errors['required'] ? 'La razón social es obligatoria' : 'Máximo 150 caracteres';
    }
    return 'Máximo 20 caracteres';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    this.form.controls.ruc.setValue(this.form.controls.ruc.value.trim());
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // muestra todos los errores
      return;
    }
    const v = this.form.getRawValue();
    const data: ClinicaRequest = {
      ruc: v.ruc,
      razonSocial: v.razonSocial.trim(),
      telefono: v.telefono.trim() || null,
      activo: v.activo,
    };
    this.guardando.set(true);
    const id = this.id();
    const peticion = id === null ? this.service.crearClinica(data) : this.service.actualizarClinica(id, data);
    peticion.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () =>
        this.router.navigate(['/interno/clinica/clinicas'], {
          state: { aviso: id === null ? 'Clínica creada.' : 'Cambios guardados.' },
        }),
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeHttp(err));
      },
    });
  }
}
```

### 1.16. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\clinica-form.html`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La pantalla del formulario. `<mat-error>` muestra *"× El RUC debe tener 11 dígitos"* y *"× La razón social es obligatoria"*.

```html
<h1>{{ id() === null ? 'Nueva clínica' : 'Editar clínica' }}</h1>
@if (loading()) {
  <p role="status">Cargando clínica…</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}

@if (!loading()) {
<form [formGroup]="form" (ngSubmit)="guardar()" class="resource-form" novalidate>
  <mat-form-field appearance="outline">
    <mat-label>RUC</mat-label>
    <input matInput formControlName="ruc" inputmode="numeric" maxlength="11" />
    <mat-error>× {{ mensaje('ruc') }}</mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Razón social</mat-label>
    <input matInput formControlName="razonSocial" maxlength="150" />
    <mat-error>× {{ mensaje('razonSocial') }}</mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Teléfono</mat-label>
    <input matInput formControlName="telefono" maxlength="20" />
    <mat-error>× {{ mensaje('telefono') }}</mat-error>
  </mat-form-field>

  <mat-checkbox formControlName="activo">Activa</mat-checkbox>

  <div class="form-actions">
    <a matButton routerLink="/interno/clinica/clinicas">Cancelar</a>
    <button matButton="filled" type="submit" [disabled]="guardando() || loading()">
      {{ guardando() ? 'Guardando…' : 'Guardar' }}
    </button>
  </div>
</form>
}
```

### 1.17. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\app.routes.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Todas las rutas del sistema, más las de clínicas (bloque con el comentario `// Módulo clínica`).

> ⚠️ Este archivo tiene las rutas de **todo** el sistema: pégalo completo.

```ts
import { authGuard, authChildGuard } from './core/guards/auth-guard';
import { rolGuard,inicioInternoGuard,publicoGuard } from './core/guards/rol-guard';
import { INTERNOS, OPERATIVOS } from './core/auth/auth-service';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'inicio',canActivate:[publicoGuard],title:'Pollería Neyos | Pollos y Parrillas',loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'destacados',canActivate:[publicoGuard],title:'Destacados de la carta | Polleria Neyos',data:{seccion:'destacados'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'promociones',canActivate:[publicoGuard],title:'Promociones | Polleria Neyos',data:{seccion:'promociones'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'acceso',canActivate:[publicoGuard],loadComponent:()=>import('./features/publico/acceso').then(m=>m.Acceso)},
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: 'sin-permiso',
    loadComponent: () => import('./core/auth/sin-permiso').then((m) => m.SinPermiso),
  },
  {
    path: 'interno',
    canActivate: [authGuard, rolGuard(...INTERNOS)],
    canActivateChild: [authChildGuard, rolGuard(...INTERNOS)],
    loadComponent: () => import('./core/layout/interno/interno').then((m) => m.Interno),
    children: [
      {path:'ubicacion',redirectTo:'portada',pathMatch:'full'},
      {path:'portada',canActivate:[rolGuard('ADMINISTRADOR')],title:'Local y página de inicio | Pollería Neyos',loadComponent:()=>import('./features/contenido/banners').then(m=>m.Banners)},
      {path:'caja',canActivate:[rolGuard('CAJERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Caja)},
      {path:'operaciones',canActivate:[rolGuard('OPERACIONES','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Operaciones)},
      {path:'mesero',canActivate:[rolGuard('MESERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Mesero)},
      // Módulo clínica (examen final práctico v7): solo ADMINISTRADOR.
      {path:'clinica',redirectTo:'clinica/clinicas',pathMatch:'full'},
      {path:'clinica/clinicas',canActivate:[rolGuard('ADMINISTRADOR')],title:'Clínicas | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-list').then(m=>m.ClinicaList)},
      {path:'clinica/clinicas/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/clinicas/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMINISTRADOR')],
        title: 'Reportes | Pollería Neyos',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        pathMatch: 'full',
        canActivate:[inicioInternoGuard],
        title: 'Portal interno | Pollería Neyos',
        loadComponent: () => import('./core/inicio/inicio-interno').then((m) => m.InicioInterno),
      },
      {
        path: 'categorias',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'categorias/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'categorias/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'productos',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'productos/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'productos/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'clientes',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'pedidos',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Pedidos | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-list').then((m) => m.PedidoList),
      },
      {
        path: 'pedidos/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Nuevo pedido | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-form').then((m) => m.PedidoForm),
      },
      {
        path: 'pedidos/:id',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Detalle del pedido | Pollería Neyos',
        loadComponent: () =>
          import('./features/pedidos/pedido-detalle').then((m) => m.PedidoDetalle),
      },

    ],
  },
  {
    path: '',
    canActivateChild: [publicoGuard],
    loadComponent: () => import('./core/layout/publico/publico').then((m) => m.Publico),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/publico/catalogo-publico').then((m) => m.CatalogoPublico),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/publico/carrito').then((m) => m.Carrito),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/perfil').then((m) => m.Perfil),
      },
      {
        path: 'checkout',
        loadComponent: () => import('./features/publico/checkout').then((m) => m.Checkout),
      },
      {path:'seguimiento/:id',data:{web:true},loadComponent:()=>import('./features/publico/mi-pedido').then(m=>m.MiPedido)},
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'mis-pedidos/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mi-pedido').then((m) => m.MiPedido),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

### 1.18. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\core\layout\interno\interno.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

El menú lateral del portal interno, ahora con la sección **Clínica** y el enlace **Clínicas**.

```html
<a class="skip-link" href="#contenido">Saltar al contenido</a>
<header class="site-header">
  <div class="container header-row">
    <a class="brand" routerLink="/interno">Polleria Neyos · Restaurant Pollos y Parrillas</a>
    <div class="session-row">
      <button type="button" class="secondary portal-toggle" #portalButton [attr.aria-expanded]="menuAbierto()"
        aria-controls="portal-menu" (click)="menuAbierto.set(!menuAbierto())">
        Menú
      </button>
      <app-navigation [interno]="true" />
    </div>
  </div>
</header>
<div class="container internal-body">
  <aside (keydown.escape)="cerrarMenu(portalButton)">

    <p class="eyebrow">Portal interno</p>
    <nav id="portal-menu" aria-label="Navegación del portal" [class.abierto]="menuAbierto()"
      (click)="menuAbierto.set(false)">
      @if(auth.tieneRol('CAJERO') && !auth.tieneRol('ADMINISTRADOR')){<a routerLink="/interno/caja"
        routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Mi caja</a><a routerLink="/interno/caja"
        [queryParams]="{vista:'pendientes'}" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos pendientes</a><a
        routerLink="/interno/caja" [queryParams]="{vista:'entregados'}" routerLinkActive="active"
        [routerLinkActiveOptions]="{exact:true}" ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos
        entregados</a>}
      @if(auth.tieneRol('OPERACIONES','ADMINISTRADOR')){<a routerLink="/interno/operaciones"
        routerLinkActive="active"><app-icono-portal nombre="cocina" />Operaciones</a>}
      @if(auth.tieneRol('MESERO','ADMINISTRADOR')){<a routerLink="/interno/mesero"
        routerLinkActive="active"><app-icono-portal nombre="mesa" />Mesero</a>}
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Gestión</span>
      <a routerLink="/interno/portada" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Local y página de inicio</a>
      <a routerLink="/interno/categorias" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Categorías</a>
      <a routerLink="/interno/productos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Productos</a>
      <a routerLink="/interno/clientes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clientes</a>
      <a routerLink="/interno/pedidos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="pedido" />Pedidos</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Clínica</span>
      <a routerLink="/interno/clinica/clinicas" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clínicas</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <a routerLink="/interno/reportes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="reporte" />Reportes</a>
      }

    </nav>
  </aside>
  <main id="contenido" tabindex="-1"><router-outlet /></main>
</div>
```

### ✅ Probar la etapa

1. **Compila el backend** desde `C:\polleriawindows\polleria-backend`. Si no muestra nada, compiló bien:

   ```bash
   .\mvnw.cmd -q -DskipTests package
   ```

2. **Mira el resultado en la página.** Si tienes abierto `desarrollo.bat`, solo espera 1 o 2 minutos a que se reconstruya el backend. Si no, desde `C:\polleriawindows`:

   ```bash
   docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml up -d --build --wait
   ```

3. Entra con `admin@polleria.pe` y pulsa `Ctrl + Shift + R`. En el menú aparece **Clínica → Clínicas**.

| Prueba | Resultado esperado |
|---|---|
| Abrir **Clínicas** | Las 3 clínicas de ejemplo |
| **+ Nueva clínica** con RUC `2013579` y razón social vacía → Guardar | *"× El RUC debe tener 11 dígitos"* y *"× La razón social es obligatoria"* |
| Crear una con RUC de 11 dígitos | "Clínica creada." y aparece en la tabla |
| **Editar** y cambiar la razón social | "Cambios guardados." |
| **Eliminar** la que creaste | "Clínica eliminada." |

---

## Etapa 2. Especialidades: listar (pregunta 2 · 1 pt)

**Archivos de esta etapa:**

| Archivo | Acción |
|---|---|
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\entity\Especialidad.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadResponse.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\mapper\EspecialidadMapper.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\repository\EspecialidadRepository.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadService.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadServiceImpl.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\controller\EspecialidadController.java` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\clinica.model.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\clinica-service.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\especialidad-list.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\app.routes.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\core\layout\interno\interno.html` | ✏️ Reemplazar todo |

### 2.1. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\entity\Especialidad.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La entidad de la tabla `EXA_CLINICA.ESPECIALIDADES`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ESPECIALIDADES", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CODIGO", nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;
}
```

### 2.2. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\dto\EspecialidadResponse.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lo que devuelve el backend: id, código y nombre.

```java
package pe.edu.upeu.polleria.clinica.especialidad.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadResponse {
    private Long id;
    private String codigo;
    private String nombre;
}
```

### 2.3. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\mapper\EspecialidadMapper.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Convierte la entidad en la respuesta.

```java
package pe.edu.upeu.polleria.clinica.especialidad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EspecialidadMapper {
    EspecialidadResponse toResponse(Especialidad especialidad);
}
```

### 2.4. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\repository\EspecialidadRepository.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

El repositorio. No necesita métodos propios.

```java
package pe.edu.upeu.polleria.clinica.especialidad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
}
```

### 2.5. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadService.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La interfaz: solo `listar`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;

public interface EspecialidadService {
    List<EspecialidadResponse> listar();
}
```

### 2.6. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\service\EspecialidadServiceImpl.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lista las especialidades ordenadas por código.

```java
package pe.edu.upeu.polleria.clinica.especialidad.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.mapper.EspecialidadMapper;
import pe.edu.upeu.polleria.clinica.especialidad.repository.EspecialidadRepository;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository repository;
    private final EspecialidadMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listar() {
        return repository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
    }
}
```

### 2.7. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\especialidad\controller\EspecialidadController.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Un solo endpoint: `GET /api/v1/clinica/especialidades`.

```java
package pe.edu.upeu.polleria.clinica.especialidad.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;
import pe.edu.upeu.polleria.clinica.especialidad.service.EspecialidadService;

@RestController
@RequestMapping("/api/v1/clinica/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final EspecialidadService service;

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
```

### 2.8. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica.model.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega la interfaz `Especialidad`.

```ts
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
```

### 2.9. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica-service.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega `listarEspecialidades()`.

```ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import {
  Clinica,
  ClinicaRequest,
  Especialidad,
} from './clinica.model';

// Un solo servicio para las tres pantallas del módulo: /api/v1/clinica/...
@Injectable({ providedIn: 'root' })
export class ClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly clinicas = '/api/v1/clinica/clinicas';

  listarClinicas() {
    return this.http.get<Clinica[]>(this.api.buildUrl(this.clinicas));
  }
  obtenerClinica(id: number) {
    return this.http.get<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
  crearClinica(data: ClinicaRequest) {
    return this.http.post<Clinica>(this.api.buildUrl(this.clinicas), data);
  }
  actualizarClinica(id: number, data: ClinicaRequest) {
    return this.http.put<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`), data);
  }
  eliminarClinica(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }

  listarEspecialidades() {
    return this.http.get<Especialidad[]>(this.api.buildUrl('/api/v1/clinica/especialidades'));
  }
}
```

### 2.10. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\especialidad-list.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La pantalla con la tabla de especialidades. El HTML va dentro del mismo archivo (`template:`).

```ts
import { Component, inject, signal } from '@angular/core';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Especialidad } from './clinica.model';

@Component({
  selector: 'app-especialidad-list',
  template: `
    <h1>Especialidades</h1>
    @if (error()) { <p role="alert" class="error">{{ error() }}</p> }
    @if (loading()) { <p role="status">Cargando especialidades…</p> }
    <div class="table-region">
      <table class="data-table">
        <caption class="sr-only">Especialidades precargadas</caption>
        <thead><tr><th scope="col">Código</th><th scope="col">Nombre</th></tr></thead>
        <tbody>
          @for (e of datos(); track e.id) {
            <tr><td data-label="Código">{{ e.codigo }}</td><td data-label="Nombre">{{ e.nombre }}</td></tr>
          } @empty {
            @if (!loading() && !error()) { <tr><td class="empty" colspan="2">No hay especialidades.</td></tr> }
          }
        </tbody>
      </table>
    </div>
  `,
})
export class EspecialidadList {
  private readonly service = inject(ClinicaService);
  readonly datos = signal<Especialidad[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  constructor() {
    this.service.listarEspecialidades().subscribe({
      next: (data) => {
        this.datos.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(mensajeHttp(err));
        this.loading.set(false);
      },
    });
  }
}
```

### 2.11. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\app.routes.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega la ruta `clinica/especialidades`.

> ⚠️ Pégalo completo.

```ts
import { authGuard, authChildGuard } from './core/guards/auth-guard';
import { rolGuard,inicioInternoGuard,publicoGuard } from './core/guards/rol-guard';
import { INTERNOS, OPERATIVOS } from './core/auth/auth-service';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'inicio',canActivate:[publicoGuard],title:'Pollería Neyos | Pollos y Parrillas',loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'destacados',canActivate:[publicoGuard],title:'Destacados de la carta | Polleria Neyos',data:{seccion:'destacados'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'promociones',canActivate:[publicoGuard],title:'Promociones | Polleria Neyos',data:{seccion:'promociones'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'acceso',canActivate:[publicoGuard],loadComponent:()=>import('./features/publico/acceso').then(m=>m.Acceso)},
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: 'sin-permiso',
    loadComponent: () => import('./core/auth/sin-permiso').then((m) => m.SinPermiso),
  },
  {
    path: 'interno',
    canActivate: [authGuard, rolGuard(...INTERNOS)],
    canActivateChild: [authChildGuard, rolGuard(...INTERNOS)],
    loadComponent: () => import('./core/layout/interno/interno').then((m) => m.Interno),
    children: [
      {path:'ubicacion',redirectTo:'portada',pathMatch:'full'},
      {path:'portada',canActivate:[rolGuard('ADMINISTRADOR')],title:'Local y página de inicio | Pollería Neyos',loadComponent:()=>import('./features/contenido/banners').then(m=>m.Banners)},
      {path:'caja',canActivate:[rolGuard('CAJERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Caja)},
      {path:'operaciones',canActivate:[rolGuard('OPERACIONES','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Operaciones)},
      {path:'mesero',canActivate:[rolGuard('MESERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Mesero)},
      // Módulo clínica (examen final práctico v7): solo ADMINISTRADOR.
      {path:'clinica',redirectTo:'clinica/clinicas',pathMatch:'full'},
      {path:'clinica/clinicas',canActivate:[rolGuard('ADMINISTRADOR')],title:'Clínicas | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-list').then(m=>m.ClinicaList)},
      {path:'clinica/clinicas/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/clinicas/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/especialidades',canActivate:[rolGuard('ADMINISTRADOR')],title:'Especialidades | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-list').then(m=>m.EspecialidadList)},
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMINISTRADOR')],
        title: 'Reportes | Pollería Neyos',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        pathMatch: 'full',
        canActivate:[inicioInternoGuard],
        title: 'Portal interno | Pollería Neyos',
        loadComponent: () => import('./core/inicio/inicio-interno').then((m) => m.InicioInterno),
      },
      {
        path: 'categorias',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'categorias/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'categorias/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'productos',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'productos/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'productos/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'clientes',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'pedidos',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Pedidos | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-list').then((m) => m.PedidoList),
      },
      {
        path: 'pedidos/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Nuevo pedido | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-form').then((m) => m.PedidoForm),
      },
      {
        path: 'pedidos/:id',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Detalle del pedido | Pollería Neyos',
        loadComponent: () =>
          import('./features/pedidos/pedido-detalle').then((m) => m.PedidoDetalle),
      },

    ],
  },
  {
    path: '',
    canActivateChild: [publicoGuard],
    loadComponent: () => import('./core/layout/publico/publico').then((m) => m.Publico),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/publico/catalogo-publico').then((m) => m.CatalogoPublico),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/publico/carrito').then((m) => m.Carrito),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/perfil').then((m) => m.Perfil),
      },
      {
        path: 'checkout',
        loadComponent: () => import('./features/publico/checkout').then((m) => m.Checkout),
      },
      {path:'seguimiento/:id',data:{web:true},loadComponent:()=>import('./features/publico/mi-pedido').then(m=>m.MiPedido)},
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'mis-pedidos/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mi-pedido').then((m) => m.MiPedido),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

### 2.12. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\core\layout\interno\interno.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Se agrega el enlace **Especialidades** al menú.

```html
<a class="skip-link" href="#contenido">Saltar al contenido</a>
<header class="site-header">
  <div class="container header-row">
    <a class="brand" routerLink="/interno">Polleria Neyos · Restaurant Pollos y Parrillas</a>
    <div class="session-row">
      <button type="button" class="secondary portal-toggle" #portalButton [attr.aria-expanded]="menuAbierto()"
        aria-controls="portal-menu" (click)="menuAbierto.set(!menuAbierto())">
        Menú
      </button>
      <app-navigation [interno]="true" />
    </div>
  </div>
</header>
<div class="container internal-body">
  <aside (keydown.escape)="cerrarMenu(portalButton)">

    <p class="eyebrow">Portal interno</p>
    <nav id="portal-menu" aria-label="Navegación del portal" [class.abierto]="menuAbierto()"
      (click)="menuAbierto.set(false)">
      @if(auth.tieneRol('CAJERO') && !auth.tieneRol('ADMINISTRADOR')){<a routerLink="/interno/caja"
        routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Mi caja</a><a routerLink="/interno/caja"
        [queryParams]="{vista:'pendientes'}" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos pendientes</a><a
        routerLink="/interno/caja" [queryParams]="{vista:'entregados'}" routerLinkActive="active"
        [routerLinkActiveOptions]="{exact:true}" ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos
        entregados</a>}
      @if(auth.tieneRol('OPERACIONES','ADMINISTRADOR')){<a routerLink="/interno/operaciones"
        routerLinkActive="active"><app-icono-portal nombre="cocina" />Operaciones</a>}
      @if(auth.tieneRol('MESERO','ADMINISTRADOR')){<a routerLink="/interno/mesero"
        routerLinkActive="active"><app-icono-portal nombre="mesa" />Mesero</a>}
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Gestión</span>
      <a routerLink="/interno/portada" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Local y página de inicio</a>
      <a routerLink="/interno/categorias" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Categorías</a>
      <a routerLink="/interno/productos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Productos</a>
      <a routerLink="/interno/clientes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clientes</a>
      <a routerLink="/interno/pedidos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="pedido" />Pedidos</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Clínica</span>
      <a routerLink="/interno/clinica/clinicas" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clínicas</a>
      <a routerLink="/interno/clinica/especialidades" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Especialidades</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <a routerLink="/interno/reportes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="reporte" />Reportes</a>
      }

    </nav>
  </aside>
  <main id="contenido" tabindex="-1"><router-outlet /></main>
</div>
```

### ✅ Probar la etapa

1. **Compila el backend** desde `C:\polleriawindows\polleria-backend`. Si no muestra nada, compiló bien:

   ```bash
   .\mvnw.cmd -q -DskipTests package
   ```

2. **Mira el resultado en la página.** Si tienes abierto `desarrollo.bat`, solo espera 1 o 2 minutos a que se reconstruya el backend. Si no, desde `C:\polleriawindows`:

   ```bash
   docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml up -d --build --wait
   ```

3. Entra con `admin@polleria.pe` y pulsa `Ctrl + Shift + R`. En el menú aparece **Especialidades**.

| Prueba | Resultado esperado |
|---|---|
| Abrir **Especialidades** | Las 5 especialidades: E-01 a E-05 |

---

## Etapa 3. Consultorios con sus especialidades (pregunta 2 · 12 pts)

El consultorio es la **cabecera** y las especialidades son sus **líneas**, como una venta con sus
productos (S04 y S09).

**Archivos de esta etapa:**

| Archivo | Acción |
|---|---|
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\entity\EstadoConsultorio.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\entity\Consultorio.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\dto\ConsultorioRequest.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\dto\ConsultorioResponse.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\mapper\ConsultorioMapper.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\repository\ConsultorioRepository.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\service\ConsultorioService.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\service\ConsultorioServiceImpl.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\controller\ConsultorioController.java` | 📄 Nuevo |
| `polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaServiceImpl.java` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\clinica.model.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\clinica-service.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\features\clinica\consultorio-list.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\consultorio-list.html` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\consultorio-form.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\consultorio-form.html` | 📄 Nuevo |
| `polleria-frontend\src\app\features\clinica\consultorio-detalle.ts` | 📄 Nuevo |
| `polleria-frontend\src\app\app.routes.ts` | ✏️ Reemplazar todo |
| `polleria-frontend\src\app\core\layout\interno\interno.html` | ✏️ Reemplazar todo |

### 3.1. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\entity\EstadoConsultorio.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

El enumerado de los 3 estados que permite el `CHECK` de la tabla.

```java
package pe.edu.upeu.polleria.clinica.consultorio.entity;

public enum EstadoConsultorio {
    DISPONIBLE, OCUPADO, MANTENIMIENTO
}
```

### 3.2. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\entity\Consultorio.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La **cabecera**. `@ManyToOne` es la clínica (columna `ID_CLINICA`). `@Enumerated(EnumType.STRING)` guarda el estado como texto. `@ManyToMany` con `@JoinTable` son las **líneas**: guarda las especialidades en `CONSULTORIO_ESPECIALIDAD` y, al eliminar el consultorio, borra primero esas filas.

```java
package pe.edu.upeu.polleria.clinica.consultorio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;

/**
 * Cabecera: el consultorio. Sus líneas (detalles) son las especialidades,
 * guardadas en la tabla intermedia CONSULTORIO_ESPECIALIDAD (muchos a muchos).
 */
@Entity
@Table(name = "CONSULTORIOS", schema = "EXA_CLINICA")
@Getter
@Setter
@NoArgsConstructor
public class Consultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ID_CLINICA", nullable = false)
    private Clinica clinica;

    @Column(name = "CODIGO", nullable = false, length = 10, unique = true)
    private String codigo;

    @Column(name = "TARIFA_HORA", nullable = false, precision = 8, scale = 2)
    private BigDecimal tarifaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoConsultorio estado = EstadoConsultorio.DISPONIBLE;

    // El consultorio es el dueño de la relación: al guardarlo se insertan las filas de
    // CONSULTORIO_ESPECIALIDAD y al eliminarlo se borran antes que la cabecera.
    @ManyToMany
    @OrderBy("codigo ASC")
    @JoinTable(name = "CONSULTORIO_ESPECIALIDAD", schema = "EXA_CLINICA",
            joinColumns = @JoinColumn(name = "ID_CONSULTORIO"),
            inverseJoinColumns = @JoinColumn(name = "ID_ESPECIALIDAD"))
    private Set<Especialidad> especialidades = new LinkedHashSet<>();
}
```

### 3.3. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\dto\ConsultorioRequest.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lo que se envía al guardar: la cabecera (`clinicaId`, `codigo`, `tarifaHora`, `estado`) más las líneas (`especialidadIds`), **en una sola petición**. Es la misma idea que `VentaRequest` con sus detalles en S04.

```java
package pe.edu.upeu.polleria.clinica.consultorio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.consultorio.entity.EstadoConsultorio;

/** Cabecera (datos del consultorio) más sus líneas: los id de las especialidades. */
@Getter
@Setter
public class ConsultorioRequest {

    @NotNull(message = "Elige la clínica")
    @Positive
    private Long clinicaId;

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 10, message = "El código admite hasta 10 caracteres")
    private String codigo;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0", inclusive = false, message = "La tarifa debe ser mayor que 0")
    @Digits(integer = 6, fraction = 2, message = "La tarifa admite hasta 6 enteros y 2 decimales")
    private BigDecimal tarifaHora;

    @NotNull(message = "Elige el estado")
    private EstadoConsultorio estado;

    @NotEmpty(message = "Agrega al menos una especialidad")
    private List<@NotNull @Positive Long> especialidadIds;
}
```

### 3.4. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\dto\ConsultorioResponse.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Lo que se devuelve: la cabecera, el nombre de la clínica y la lista de especialidades.

```java
package pe.edu.upeu.polleria.clinica.consultorio.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.polleria.clinica.especialidad.dto.EspecialidadResponse;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultorioResponse {
    private Long id;
    private Long clinicaId;
    private String clinicaRazonSocial;
    private String codigo;
    private BigDecimal tarifaHora;
    private String estado;
    private List<EspecialidadResponse> especialidades;
}
```

### 3.5. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\mapper\ConsultorioMapper.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Convierte la entidad en la respuesta. `uses = EspecialidadMapper.class` reutiliza el mapper de especialidades para la lista.

```java
package pe.edu.upeu.polleria.clinica.consultorio.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;
import pe.edu.upeu.polleria.clinica.especialidad.mapper.EspecialidadMapper;

@Mapper(componentModel = "spring", uses = EspecialidadMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ConsultorioMapper {

    @Mapping(target = "clinicaId", source = "clinica.id")
    @Mapping(target = "clinicaRazonSocial", source = "clinica.razonSocial")
    ConsultorioResponse toResponse(Consultorio consultorio);
}
```

### 3.6. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\repository\ConsultorioRepository.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

`@EntityGraph` trae la clínica y las especialidades en una sola consulta (evita el problema N+1). `existsByClinicaId` lo usa `ClinicaServiceImpl`.

```java
package pe.edu.upeu.polleria.clinica.consultorio.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {

    // Trae la clínica y las especialidades en la misma consulta (evita el problema N+1).
    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    List<Consultorio> findAll(Sort sort);

    @Override
    @EntityGraph(attributePaths = {"clinica", "especialidades"})
    Optional<Consultorio> findById(Long id);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByClinicaId(Long clinicaId);
}
```

### 3.7. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\service\ConsultorioService.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La interfaz: listar, obtener, crear y eliminar.

```java
package pe.edu.upeu.polleria.clinica.consultorio.service;

import java.util.List;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;

public interface ConsultorioService {
    List<ConsultorioResponse> listar();

    ConsultorioResponse obtener(Long id);

    ConsultorioResponse crear(ConsultorioRequest request);

    void eliminar(Long id);
}
```

### 3.8. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\service\ConsultorioServiceImpl.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

`crear` es `@Transactional`: valida que el código no se repita y que existan la clínica y las especialidades, y guarda la cabecera con sus líneas **todo junto**. Si algo falla, no se guarda nada (S04).

```java
package pe.edu.upeu.polleria.clinica.consultorio.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.clinica.repository.ClinicaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.entity.Consultorio;
import pe.edu.upeu.polleria.clinica.consultorio.mapper.ConsultorioMapper;
import pe.edu.upeu.polleria.clinica.consultorio.repository.ConsultorioRepository;
import pe.edu.upeu.polleria.clinica.especialidad.entity.Especialidad;
import pe.edu.upeu.polleria.clinica.especialidad.repository.EspecialidadRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.InvalidRequestException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ConsultorioServiceImpl implements ConsultorioService {

    private final ConsultorioRepository consultorioRepository;
    private final ClinicaRepository clinicaRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ConsultorioMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listar() {
        return consultorioRepository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultorioResponse obtener(Long id) {
        return mapper.toResponse(buscarOFallar(id));
    }

    /**
     * Registra la cabecera y sus líneas en una sola transacción: si una
     * especialidad no existe, no se guarda nada (rollback).
     */
    @Override
    @Transactional
    public ConsultorioResponse crear(ConsultorioRequest request) {
        String codigo = request.getCodigo().strip().toUpperCase();
        if (consultorioRepository.existsByCodigoIgnoreCase(codigo))
            throw new BusinessConflictException("Ya existe un consultorio con el código " + codigo);

        Clinica clinica = clinicaRepository.findById(request.getClinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clínica no encontrada: " + request.getClinicaId()));

        Set<Long> ids = new LinkedHashSet<>(request.getEspecialidadIds()); // quita repetidos
        List<Especialidad> especialidades = especialidadRepository.findAllById(ids);
        if (especialidades.size() != ids.size())
            throw new InvalidRequestException("Alguna especialidad elegida no existe");

        Consultorio consultorio = new Consultorio();
        consultorio.setClinica(clinica);
        consultorio.setCodigo(codigo);
        consultorio.setTarifaHora(request.getTarifaHora());
        consultorio.setEstado(request.getEstado());
        consultorio.getEspecialidades().addAll(especialidades);

        Consultorio guardado = consultorioRepository.save(consultorio);
        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Consultorio consultorio = buscarOFallar(id);
        // Hibernate borra primero sus filas de CONSULTORIO_ESPECIALIDAD y después la cabecera.
        consultorioRepository.delete(consultorio);
    }

    private Consultorio buscarOFallar(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio no encontrado: " + id));
    }
}
```

### 3.9. 📄 ARCHIVO NUEVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\consultorio\controller\ConsultorioController.java`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Los endpoints en `/api/v1/clinica/consultorios`: GET lista, GET por id, POST y DELETE.

```java
package pe.edu.upeu.polleria.clinica.consultorio.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioRequest;
import pe.edu.upeu.polleria.clinica.consultorio.dto.ConsultorioResponse;
import pe.edu.upeu.polleria.clinica.consultorio.service.ConsultorioService;

@RestController
@RequestMapping("/api/v1/clinica/consultorios")
@RequiredArgsConstructor
public class ConsultorioController {

    private final ConsultorioService service;

    @GetMapping
    public ResponseEntity<List<ConsultorioResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultorioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ConsultorioResponse> crear(@Valid @RequestBody ConsultorioRequest request) {
        ConsultorioResponse response = service.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/clinica/consultorios/" + response.getId())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### 3.10. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-backend\src\main\java\pe\edu\upeu\polleria\clinica\clinica\service\ClinicaServiceImpl.java`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

**Ya lo creaste en la etapa 1.** Ahora `eliminar` revisa si la clínica tiene consultorios y, si los tiene, responde con un mensaje claro en lugar de un error de base de datos.

```java
package pe.edu.upeu.polleria.clinica.clinica.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaRequest;
import pe.edu.upeu.polleria.clinica.clinica.dto.ClinicaResponse;
import pe.edu.upeu.polleria.clinica.clinica.entity.Clinica;
import pe.edu.upeu.polleria.clinica.clinica.mapper.ClinicaMapper;
import pe.edu.upeu.polleria.clinica.clinica.repository.ClinicaRepository;
import pe.edu.upeu.polleria.clinica.consultorio.repository.ConsultorioRepository;
import pe.edu.upeu.polleria.exception.BusinessConflictException;
import pe.edu.upeu.polleria.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ClinicaServiceImpl implements ClinicaService {

    private final ClinicaRepository clinicaRepository;
    private final ConsultorioRepository consultorioRepository;
    private final ClinicaMapper clinicaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClinicaResponse> listar() {
        return clinicaRepository.findAll(Sort.by("razonSocial")).stream().map(clinicaMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicaResponse obtener(Long id) {
        return clinicaMapper.toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional
    public ClinicaResponse crear(ClinicaRequest request) {
        normalizar(request);
        if (clinicaRepository.existsByRuc(request.getRuc()))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        Clinica clinica = clinicaMapper.toEntity(request);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public ClinicaResponse actualizar(Long id, ClinicaRequest request) {
        Clinica clinica = buscarOFallar(id);
        normalizar(request);
        if (clinicaRepository.existsByRucAndIdNot(request.getRuc(), id))
            throw new BusinessConflictException("Ya existe una clínica con ese RUC");
        clinicaMapper.actualizar(request, clinica);
        return clinicaMapper.toResponse(clinicaRepository.save(clinica));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Clinica clinica = buscarOFallar(id);
        // La clave foránea CONSULTORIOS.ID_CLINICA impediría borrarla: se avisa con un mensaje claro.
        if (consultorioRepository.existsByClinicaId(id))
            throw new BusinessConflictException("No se puede eliminar: la clínica tiene consultorios registrados");
        clinicaRepository.delete(clinica);
    }

    private Clinica buscarOFallar(Long id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clínica no encontrada: " + id));
    }

    private void normalizar(ClinicaRequest request) {
        request.setRuc(request.getRuc().strip());
        request.setRazonSocial(request.getRazonSocial().strip());
        if (request.getTelefono() != null) {
            String telefono = request.getTelefono().strip();
            request.setTelefono(telefono.isEmpty() ? null : telefono);
        }
    }
}
```

### 3.11. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica.model.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Versión final: se agregan `Consultorio`, `ConsultorioRequest` y los estados.

```ts
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
```

### 3.12. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\features\clinica\clinica-service.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Versión final: se agregan los métodos de consultorios.

```ts
import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApiService } from '../../core/services/api-service';
import {
  Clinica,
  ClinicaRequest,
  Consultorio,
  ConsultorioRequest,
  Especialidad,
} from './clinica.model';

// Un solo servicio para las tres pantallas del módulo: /api/v1/clinica/...
@Injectable({ providedIn: 'root' })
export class ClinicaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly clinicas = '/api/v1/clinica/clinicas';
  private readonly consultorios = '/api/v1/clinica/consultorios';

  listarClinicas() {
    return this.http.get<Clinica[]>(this.api.buildUrl(this.clinicas));
  }
  obtenerClinica(id: number) {
    return this.http.get<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }
  crearClinica(data: ClinicaRequest) {
    return this.http.post<Clinica>(this.api.buildUrl(this.clinicas), data);
  }
  actualizarClinica(id: number, data: ClinicaRequest) {
    return this.http.put<Clinica>(this.api.buildUrl(`${this.clinicas}/${id}`), data);
  }
  eliminarClinica(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.clinicas}/${id}`));
  }

  listarEspecialidades() {
    return this.http.get<Especialidad[]>(this.api.buildUrl('/api/v1/clinica/especialidades'));
  }

  listarConsultorios() {
    return this.http.get<Consultorio[]>(this.api.buildUrl(this.consultorios));
  }
  obtenerConsultorio(id: number) {
    return this.http.get<Consultorio>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
  crearConsultorio(data: ConsultorioRequest) {
    return this.http.post<Consultorio>(this.api.buildUrl(this.consultorios), data);
  }
  eliminarConsultorio(id: number) {
    return this.http.delete<void>(this.api.buildUrl(`${this.consultorios}/${id}`));
  }
}
```

### 3.13. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\consultorio-list.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La lógica del listado de consultorios (Figura 4), con **Eliminar**.

```ts
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmarEliminacion } from '../../shared/confirmar-eliminacion';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Consultorio } from './clinica.model';

@Component({
  selector: 'app-consultorio-list',
  imports: [RouterLink, MatButtonModule, DecimalPipe],
  templateUrl: './consultorio-list.html',
})
export class ConsultorioList {
  private readonly service = inject(ClinicaService);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly datos = signal<Consultorio[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly aviso = signal((history.state?.['aviso'] as string) || '');

  constructor() {
    this.cargar();
  }

  cargar() {
    this.loading.set(true);
    this.error.set('');
    this.service
      .listarConsultorios()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (data) => {
          this.datos.set(data);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(mensajeHttp(err));
          this.loading.set(false);
        },
      });
  }

  eliminar(item: Consultorio) {
    this.dialog
      .open(ConfirmarEliminacion, {
        data: `el consultorio ${item.codigo} y sus especialidades asignadas`,
        width: '26rem',
        maxWidth: 'calc(100vw - 2rem)',
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmado) => {
        if (confirmado !== true) return;
        this.error.set('');
        this.aviso.set('');
        this.service
          .eliminarConsultorio(item.id)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.aviso.set(`Consultorio ${item.codigo} eliminado.`);
              this.cargar();
            },
            error: (err) => this.error.set(mensajeHttp(err)),
          });
      });
  }
}
```

### 3.14. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\consultorio-list.html`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La tabla de consultorios con **+ Nuevo consultorio**, **Ver** y **Eliminar**.

```html
<div class="page-heading">
  <h1>Consultorios</h1>
  <a matButton="filled" routerLink="/interno/clinica/consultorios/nuevo">+ Nuevo consultorio</a>
</div>

@if (aviso()) {
  <p role="status" class="notice">{{ aviso() }}</p>
}
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}
@if (loading()) {
  <p role="status">Cargando consultorios…</p>
}

<div class="table-region" [attr.aria-busy]="loading()">
  <table class="data-table">
    <caption class="sr-only">Listado de consultorios</caption>
    <thead>
      <tr>
        <th scope="col">Código</th>
        <th scope="col">Clínica</th>
        <th scope="col">Tarifa</th>
        <th scope="col">Estado</th>
        <th scope="col">Acciones</th>
      </tr>
    </thead>
    <tbody>
      @for (item of datos(); track item.id) {
        <tr>
          <td data-label="Código">{{ item.codigo }}</td>
          <td data-label="Clínica">{{ item.clinicaRazonSocial }}</td>
          <td data-label="Tarifa">{{ item.tarifaHora | number: '1.2-2' }}</td>
          <td data-label="Estado">{{ item.estado }}</td>
          <td data-label="Acciones" class="row-actions">
            <a matButton [routerLink]="['/interno/clinica/consultorios', item.id]">Ver</a>
            <button matButton class="danger" type="button" (click)="eliminar(item)">Eliminar</button>
          </td>
        </tr>
      } @empty {
        @if (!loading() && !error()) {
          <tr><td class="empty" colspan="5">No hay consultorios registrados.</td></tr>
        }
      }
    </tbody>
  </table>
</div>
```

### 3.15. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\consultorio-form.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

El formulario de cabecera con detalles (Figura 5). La cabecera es un formulario reactivo y las líneas son la `signal` `asignadas`. `agregar()` y `quitar()` modifican esa lista, y `disponibles` oculta las especialidades que ya agregaste. Al guardar se envía todo en una sola petición.

```ts
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { mensajeHttp } from '../../shared/http-error';
import { precioMonetario, textoObligatorio } from '../../shared/form-validation';
import { ClinicaService } from './clinica-service';
import { Clinica, ConsultorioRequest, ESTADOS_CONSULTORIO, Especialidad, EstadoConsultorio } from './clinica.model';

/**
 * Cabecera con detalles: el formulario reactivo es la cabecera (clínica, código,
 * tarifa y estado) y la señal `asignadas` son las líneas (especialidades).
 * Al guardar se envía todo junto en una sola petición.
 */
@Component({
  selector: 'app-consultorio-form',
  imports: [ReactiveFormsModule, FormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './consultorio-form.html',
  styles: `.lineas{grid-column:1/-1;border:1px solid var(--mat-sys-outline-variant,#ccc);border-radius:8px;padding:1rem;display:grid;gap:.75rem}
    .lineas legend{font-weight:600;padding:0 .25rem}
    .agregar-linea{display:flex;flex-wrap:wrap;gap:.75rem;align-items:center}
    .agregar-linea mat-form-field{flex:1 1 14rem}`,
})
export class ConsultorioForm {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ClinicaService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly estados = ESTADOS_CONSULTORIO;
  readonly clinicas = signal<Clinica[]>([]);
  readonly especialidades = signal<Especialidad[]>([]);
  readonly asignadas = signal<Especialidad[]>([]); // las líneas
  readonly elegidaId = signal<number | null>(null); // especialidad en la lista desplegable
  readonly guardando = signal(false);
  readonly error = signal('');
  readonly errorLineas = signal('');

  // Solo se ofrecen las especialidades que aún no se agregaron.
  readonly disponibles = computed(() => {
    const usadas = new Set(this.asignadas().map((e) => e.id));
    return this.especialidades().filter((e) => !usadas.has(e.id));
  });

  readonly form = this.fb.group({
    clinicaId: this.fb.control<number | null>(null, Validators.required),
    codigo: this.fb.nonNullable.control('', [textoObligatorio, Validators.maxLength(10)]),
    tarifaHora: this.fb.nonNullable.control('', [precioMonetario]),
    estado: this.fb.nonNullable.control<EstadoConsultorio>('DISPONIBLE', Validators.required),
  });

  constructor() {
    this.service.listarClinicas().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.clinicas.set(data.filter((c) => c.activo)),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
    this.service.listarEspecialidades().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this.especialidades.set(data),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
  }

  agregar() {
    const id = this.elegidaId();
    const especialidad = this.especialidades().find((e) => e.id === id);
    if (!especialidad) {
      this.errorLineas.set('Elige una especialidad de la lista.');
      return;
    }
    this.asignadas.update((lista) => [...lista, especialidad]);
    this.elegidaId.set(null);
    this.errorLineas.set('');
  }

  quitar(especialidad: Especialidad) {
    this.asignadas.update((lista) => lista.filter((e) => e.id !== especialidad.id));
  }

  mensaje(campo: 'clinicaId' | 'codigo' | 'tarifaHora'): string {
    const control = this.form.controls[campo];
    if (!control.touched || !control.errors) return '';
    if (campo === 'clinicaId') return 'Elige la clínica';
    if (campo === 'codigo') return control.errors['maxlength'] ? 'Máximo 10 caracteres' : 'El código es obligatorio';
    return 'Ingresa una tarifa mayor que 0, con hasta 2 decimales';
  }

  guardar() {
    if (this.guardando()) return;
    this.error.set('');
    const sinLineas = this.asignadas().length === 0;
    this.errorLineas.set(sinLineas ? 'Agrega al menos una especialidad.' : '');
    if (this.form.invalid || sinLineas) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const data: ConsultorioRequest = {
      clinicaId: v.clinicaId!,
      codigo: v.codigo.trim().toUpperCase(),
      tarifaHora: Number(v.tarifaHora),
      estado: v.estado,
      especialidadIds: this.asignadas().map((e) => e.id),
    };
    this.guardando.set(true);
    this.service
      .crearConsultorio(data)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (c) =>
          this.router.navigate(['/interno/clinica/consultorios'], {
            state: { aviso: `Consultorio ${c.codigo} registrado con ${c.especialidades.length} especialidad(es).` },
          }),
        error: (err) => {
          this.guardando.set(false);
          this.error.set(mensajeHttp(err));
        },
      });
  }
}
```

### 3.16. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\consultorio-form.html`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

La pantalla: listas desplegables de clínica y estado, el selector de especialidad con **Agregar**, y la tabla de asignadas con **Quitar**.

```html
<h1>Nuevo consultorio</h1>
@if (error()) {
  <p role="alert" class="error">{{ error() }}</p>
}

<form [formGroup]="form" (ngSubmit)="guardar()" class="resource-form" novalidate>
  <!-- Cabecera -->
  <mat-form-field appearance="outline">
    <mat-label>Clínica</mat-label>
    <mat-select formControlName="clinicaId">
      @for (c of clinicas(); track c.id) {
        <mat-option [value]="c.id">{{ c.razonSocial }}</mat-option>
      }
    </mat-select>
    <mat-error>× {{ mensaje('clinicaId') }}</mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Código</mat-label>
    <input matInput formControlName="codigo" maxlength="10" placeholder="CON-301" />
    <mat-error>× {{ mensaje('codigo') }}</mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Tarifa por hora (S/)</mat-label>
    <input matInput formControlName="tarifaHora" inputmode="decimal" placeholder="80.00" />
    <mat-error>× {{ mensaje('tarifaHora') }}</mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Estado</mat-label>
    <mat-select formControlName="estado">
      @for (e of estados; track e) {
        <mat-option [value]="e">{{ e }}</mat-option>
      }
    </mat-select>
  </mat-form-field>

  <!-- Líneas: especialidades del consultorio -->
  <fieldset class="lineas">
    <legend>Especialidades del consultorio</legend>
    <div class="agregar-linea">
      <mat-form-field appearance="outline">
        <mat-label>Especialidad</mat-label>
        <mat-select [ngModel]="elegidaId()" (ngModelChange)="elegidaId.set($event)" [ngModelOptions]="{ standalone: true }">
          @for (e of disponibles(); track e.id) {
            <mat-option [value]="e.id">{{ e.nombre }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
      <button matButton="outlined" type="button" (click)="agregar()">Agregar</button>
    </div>
    @if (errorLineas()) {
      <p role="alert" class="error">× {{ errorLineas() }}</p>
    }

    <table class="data-table">
      <caption class="sr-only">Especialidades asignadas</caption>
      <thead><tr><th scope="col">Código</th><th scope="col">Especialidad</th><th scope="col">Acción</th></tr></thead>
      <tbody>
        @for (e of asignadas(); track e.id) {
          <tr>
            <td data-label="Código">{{ e.codigo }}</td>
            <td data-label="Especialidad">{{ e.nombre }}</td>
            <td data-label="Acción"><button matButton class="danger" type="button" (click)="quitar(e)">Quitar</button></td>
          </tr>
        } @empty {
          <tr><td class="empty" colspan="3">Todavía no agregaste especialidades.</td></tr>
        }
      </tbody>
    </table>
  </fieldset>

  <div class="form-actions">
    <a matButton routerLink="/interno/clinica/consultorios">Cancelar</a>
    <button matButton="filled" type="submit" [disabled]="guardando()">
      {{ guardando() ? 'Guardando…' : 'Guardar' }}
    </button>
  </div>
</form>
```

### 3.17. 📄 ARCHIVO NUEVO

`polleria-frontend\src\app\features\clinica\consultorio-detalle.ts`

Crea el archivo (clic derecho en la carpeta → **New File**; si la carpeta no existe, créala con **New Folder**) y pega el código.

Ver consultorio en solo lectura (Figura 6): la cabecera y su tabla de especialidades.

```ts
import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { mensajeHttp } from '../../shared/http-error';
import { ClinicaService } from './clinica-service';
import { Consultorio } from './clinica.model';

// Solo lectura: la cabecera y su tabla de especialidades asignadas.
@Component({
  selector: 'app-consultorio-detalle',
  imports: [RouterLink, MatButtonModule, DecimalPipe],
  template: `
    @if (error()) { <p role="alert" class="error">{{ error() }}</p> }
    @if (consultorio(); as c) {
      <h1>Consultorio {{ c.codigo }}</h1>
      <dl class="detalle-cabecera">
        <div><dt>Clínica</dt><dd>{{ c.clinicaRazonSocial }}</dd></div>
        <div><dt>Tarifa por hora</dt><dd>{{ c.tarifaHora | number: '1.2-2' }}</dd></div>
        <div><dt>Estado</dt><dd>{{ c.estado }}</dd></div>
      </dl>
      <h2>Especialidades asignadas</h2>
      <table class="data-table">
        <caption class="sr-only">Especialidades del consultorio {{ c.codigo }}</caption>
        <thead><tr><th scope="col">Código</th><th scope="col">Especialidad</th></tr></thead>
        <tbody>
          @for (e of c.especialidades; track e.id) {
            <tr><td data-label="Código">{{ e.codigo }}</td><td data-label="Especialidad">{{ e.nombre }}</td></tr>
          } @empty {
            <tr><td class="empty" colspan="2">Sin especialidades asignadas.</td></tr>
          }
        </tbody>
      </table>
    } @else if (!error()) {
      <p role="status">Cargando consultorio…</p>
    }
    <div class="form-actions"><a matButton routerLink="/interno/clinica/consultorios">Volver</a></div>
  `,
  styles: `.detalle-cabecera{display:grid;grid-template-columns:repeat(auto-fit,minmax(12rem,1fr));gap:.75rem;margin:1rem 0}
    .detalle-cabecera dt{font-weight:600}.detalle-cabecera dd{margin:0}`,
})
export class ConsultorioDetalle {
  private readonly service = inject(ClinicaService);
  readonly consultorio = signal<Consultorio | null>(null);
  readonly error = signal('');

  constructor() {
    const id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
    this.service.obtenerConsultorio(id).subscribe({
      next: (data) => this.consultorio.set(data),
      error: (err) => this.error.set(mensajeHttp(err)),
    });
  }
}
```

### 3.18. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\app.routes.ts`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Versión final: se agregan las rutas de consultorios.

> ⚠️ Pégalo completo.

```ts
import { authGuard, authChildGuard } from './core/guards/auth-guard';
import { rolGuard,inicioInternoGuard,publicoGuard } from './core/guards/rol-guard';
import { INTERNOS, OPERATIVOS } from './core/auth/auth-service';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {path:'inicio',canActivate:[publicoGuard],title:'Pollería Neyos | Pollos y Parrillas',loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'destacados',canActivate:[publicoGuard],title:'Destacados de la carta | Polleria Neyos',data:{seccion:'destacados'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'promociones',canActivate:[publicoGuard],title:'Promociones | Polleria Neyos',data:{seccion:'promociones'},loadComponent:()=>import('./core/inicio/inicio-publico').then(m=>m.InicioPublico)},
  {path:'',pathMatch:'full',redirectTo:'inicio'},
  {path:'acceso',canActivate:[publicoGuard],loadComponent:()=>import('./features/publico/acceso').then(m=>m.Acceso)},
  {
    path: 'auth/callback',
    loadComponent: () => import('./core/auth/auth-callback').then((m) => m.AuthCallback),
  },
  {
    path: 'sin-permiso',
    loadComponent: () => import('./core/auth/sin-permiso').then((m) => m.SinPermiso),
  },
  {
    path: 'interno',
    canActivate: [authGuard, rolGuard(...INTERNOS)],
    canActivateChild: [authChildGuard, rolGuard(...INTERNOS)],
    loadComponent: () => import('./core/layout/interno/interno').then((m) => m.Interno),
    children: [
      {path:'ubicacion',redirectTo:'portada',pathMatch:'full'},
      {path:'portada',canActivate:[rolGuard('ADMINISTRADOR')],title:'Local y página de inicio | Pollería Neyos',loadComponent:()=>import('./features/contenido/banners').then(m=>m.Banners)},
      {path:'caja',canActivate:[rolGuard('CAJERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Caja)},
      {path:'operaciones',canActivate:[rolGuard('OPERACIONES','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Operaciones)},
      {path:'mesero',canActivate:[rolGuard('MESERO','ADMINISTRADOR')],loadComponent:()=>import('./features/operacion/paneles').then(m=>m.Mesero)},
      // Módulo clínica (examen final práctico v7): solo ADMINISTRADOR.
      {path:'clinica',redirectTo:'clinica/clinicas',pathMatch:'full'},
      {path:'clinica/clinicas',canActivate:[rolGuard('ADMINISTRADOR')],title:'Clínicas | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-list').then(m=>m.ClinicaList)},
      {path:'clinica/clinicas/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nueva clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/clinicas/:id/editar',canActivate:[rolGuard('ADMINISTRADOR')],title:'Editar clínica | Pollería Neyos',loadComponent:()=>import('./features/clinica/clinica-form').then(m=>m.ClinicaForm)},
      {path:'clinica/especialidades',canActivate:[rolGuard('ADMINISTRADOR')],title:'Especialidades | Pollería Neyos',loadComponent:()=>import('./features/clinica/especialidad-list').then(m=>m.EspecialidadList)},
      {path:'clinica/consultorios',canActivate:[rolGuard('ADMINISTRADOR')],title:'Consultorios | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-list').then(m=>m.ConsultorioList)},
      {path:'clinica/consultorios/nuevo',canActivate:[rolGuard('ADMINISTRADOR')],title:'Nuevo consultorio | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-form').then(m=>m.ConsultorioForm)},
      {path:'clinica/consultorios/:id',canActivate:[rolGuard('ADMINISTRADOR')],title:'Consultorio | Pollería Neyos',loadComponent:()=>import('./features/clinica/consultorio-detalle').then(m=>m.ConsultorioDetalle)},
      {
        path: 'reportes',
        canActivate: [rolGuard('ADMINISTRADOR')],
        title: 'Reportes | Pollería Neyos',
        loadComponent: () => import('./features/reportes/reportes').then((m) => m.Reportes),
      },
      {
        path: '',
        pathMatch: 'full',
        canActivate:[inicioInternoGuard],
        title: 'Portal interno | Pollería Neyos',
        loadComponent: () => import('./core/inicio/inicio-interno').then((m) => m.InicioInterno),
      },
      {
        path: 'categorias',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-list').then((m) => m.CategoriaList),
      },
      {
        path: 'categorias/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'categorias/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/categoria/categoria-form').then((m) => m.CategoriaForm),
      },
      {
        path: 'productos',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-list').then((m) => m.ProductoList),
      },
      {
        path: 'productos/nuevo',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'productos/:id/editar',
        canActivate: [rolGuard('ADMINISTRADOR')],
        loadComponent: () =>
          import('./features/catalogo/producto/producto-form').then((m) => m.ProductoForm),
      },
      {
        path: 'clientes',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-list').then((m) => m.ClienteList),
      },
      {
        path: 'clientes/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'clientes/:id/editar',
        canActivate: [rolGuard(...OPERATIVOS)],
        loadComponent: () =>
          import('./features/clientes/cliente/cliente-form').then((m) => m.ClienteForm),
      },
      {
        path: 'pedidos',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Pedidos | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-list').then((m) => m.PedidoList),
      },
      {
        path: 'pedidos/nuevo',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Nuevo pedido | Pollería Neyos',
        loadComponent: () => import('./features/pedidos/pedido-form').then((m) => m.PedidoForm),
      },
      {
        path: 'pedidos/:id',
        canActivate: [rolGuard(...OPERATIVOS)],
        title: 'Detalle del pedido | Pollería Neyos',
        loadComponent: () =>
          import('./features/pedidos/pedido-detalle').then((m) => m.PedidoDetalle),
      },

    ],
  },
  {
    path: '',
    canActivateChild: [publicoGuard],
    loadComponent: () => import('./core/layout/publico/publico').then((m) => m.Publico),
    children: [
      {
        path: 'catalogo',
        loadComponent: () =>
          import('./features/publico/catalogo-publico').then((m) => m.CatalogoPublico),
      },
      {
        path: 'carrito',
        loadComponent: () => import('./features/publico/carrito').then((m) => m.Carrito),
      },
      {
        path: 'perfil',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/perfil').then((m) => m.Perfil),
      },
      {
        path: 'checkout',
        loadComponent: () => import('./features/publico/checkout').then((m) => m.Checkout),
      },
      {path:'seguimiento/:id',data:{web:true},loadComponent:()=>import('./features/publico/mi-pedido').then(m=>m.MiPedido)},
      {
        path: 'mis-pedidos',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mis-pedidos').then((m) => m.MisPedidos),
      },
      {
        path: 'mis-pedidos/:id',
        canActivate: [authGuard, rolGuard('CLIENTE')],
        loadComponent: () => import('./features/publico/mi-pedido').then((m) => m.MiPedido),
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'inicio',
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
```

### 3.19. ✏️ REEMPLAZAR TODO EL ARCHIVO

`polleria-frontend\src\app\core\layout\interno\interno.html`

Abre el archivo, selecciona todo (`Ctrl + A`), bórralo y pega este código.

Versión final: se agrega el enlace **Consultorios**.

```html
<a class="skip-link" href="#contenido">Saltar al contenido</a>
<header class="site-header">
  <div class="container header-row">
    <a class="brand" routerLink="/interno">Polleria Neyos · Restaurant Pollos y Parrillas</a>
    <div class="session-row">
      <button type="button" class="secondary portal-toggle" #portalButton [attr.aria-expanded]="menuAbierto()"
        aria-controls="portal-menu" (click)="menuAbierto.set(!menuAbierto())">
        Menú
      </button>
      <app-navigation [interno]="true" />
    </div>
  </div>
</header>
<div class="container internal-body">
  <aside (keydown.escape)="cerrarMenu(portalButton)">

    <p class="eyebrow">Portal interno</p>
    <nav id="portal-menu" aria-label="Navegación del portal" [class.abierto]="menuAbierto()"
      (click)="menuAbierto.set(false)">
      @if(auth.tieneRol('CAJERO') && !auth.tieneRol('ADMINISTRADOR')){<a routerLink="/interno/caja"
        routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Mi caja</a><a routerLink="/interno/caja"
        [queryParams]="{vista:'pendientes'}" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}"
        ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos pendientes</a><a
        routerLink="/interno/caja" [queryParams]="{vista:'entregados'}" routerLinkActive="active"
        [routerLinkActiveOptions]="{exact:true}" ariaCurrentWhenActive="page"><app-icono-portal nombre="caja" />Pedidos
        entregados</a>}
      @if(auth.tieneRol('OPERACIONES','ADMINISTRADOR')){<a routerLink="/interno/operaciones"
        routerLinkActive="active"><app-icono-portal nombre="cocina" />Operaciones</a>}
      @if(auth.tieneRol('MESERO','ADMINISTRADOR')){<a routerLink="/interno/mesero"
        routerLinkActive="active"><app-icono-portal nombre="mesa" />Mesero</a>}
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Gestión</span>
      <a routerLink="/interno/portada" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Local y página de inicio</a>
      <a routerLink="/interno/categorias" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Categorías</a>
      <a routerLink="/interno/productos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Productos</a>
      <a routerLink="/interno/clientes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clientes</a>
      <a routerLink="/interno/pedidos" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="pedido" />Pedidos</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <span class="nav-section">Clínica</span>
      <a routerLink="/interno/clinica/clinicas" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="cliente" />Clínicas</a>
      <a routerLink="/interno/clinica/especialidades" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="categoria" />Especialidades</a>
      <a routerLink="/interno/clinica/consultorios" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="producto" />Consultorios</a>
      }
      @if (auth.tieneRol('ADMINISTRADOR')) {
      <a routerLink="/interno/reportes" routerLinkActive="active" ariaCurrentWhenActive="page"><app-icono-portal
          nombre="reporte" />Reportes</a>
      }

    </nav>
  </aside>
  <main id="contenido" tabindex="-1"><router-outlet /></main>
</div>
```

### ✅ Probar la etapa

1. **Compila el backend** desde `C:\polleriawindows\polleria-backend`. Si no muestra nada, compiló bien:

   ```bash
   .\mvnw.cmd -q -DskipTests package
   ```

2. **Mira el resultado en la página.** Si tienes abierto `desarrollo.bat`, solo espera 1 o 2 minutos a que se reconstruya el backend. Si no, desde `C:\polleriawindows`:

   ```bash
   docker compose --env-file .env.prodtest -f compose.prod.yml -f compose.prodtest.yml up -d --build --wait
   ```

3. Entra con `admin@polleria.pe` y pulsa `Ctrl + Shift + R`. En el menú aparece **Consultorios**.

| Prueba (Tabla 3 del examen) | Resultado esperado |
|---|---|
| Abrir **Consultorios** | CON-101, CON-102, CON-201 y CON-202 |
| **+ Nuevo consultorio**: clínica de la lista, código `CON-777`, tarifa `85.50` y estado | Se llenan sin errores |
| Agregar **Medicina general** y **Cardiologia**, quitar **Medicina general** y guardar | "Consultorio CON-777 registrado con 1 especialidad(es)." |
| **Ver** CON-777 | La cabecera y la tabla con **Cardiologia** |
| **Eliminar** CON-777 desde el listado | "Consultorio CON-777 eliminado." |
| En **Clínicas**, eliminar *Clinica Altiplano SAC* | *"No se puede eliminar: la clínica tiene consultorios registrados"* |

---

## Al terminar la práctica

Tu rama `practica-clinica` quedó igual que `main`. Para volver a `main` y borrar la rama de
práctica:

```bash
git checkout main
```

```bash
git branch -D practica-clinica
```

Para practicar otra vez, repite desde la etapa 0.

> Si en algún momento quieres **descartar** lo que hiciste en la práctica sin terminarla, usa
> `git checkout -- .` (descarta los cambios de los archivos que ya existían) y luego los dos
> comandos de arriba.

---

## Errores frecuentes

| Síntoma | Causa |
|---|---|
| El backend no arranca: `wrong column type ... found [int2] ... expecting [integer]` | Falta `@JdbcTypeCode(SqlTypes.SMALLINT)` en `Clinica.java` |
| El backend no arranca: `wrong column type ... found [bpchar]` | Falta `@JdbcTypeCode(SqlTypes.CHAR)` en el RUC de `Clinica.java` |
| Todas las pantallas del módulo dicen "sin permiso" o responden 403 | `SecurityConfig.java` no quedó con las 2 líneas de la etapa 1 |
| Al compilar: `cannot find symbol ... ConsultorioRepository` en la etapa 1 | Pegaste el `ClinicaServiceImpl` de la etapa 3; en la etapa 1 va la versión sin consultorios |
| Al compilar: `Unmapped target property` | Un mapper no quedó completo |
| Al pulsar un enlace del menú vuelve al inicio | `app.routes.ts` no quedó completo para esa etapa |
| `NG2008: Could not find template file` | Falta un archivo `.html` nuevo de la etapa |
| El estado del consultorio da error de `CHECK` | Falta `@Enumerated(EnumType.STRING)` en `Consultorio.java` |
| La página sigue igual | No reconstruiste, o falta `Ctrl + Shift + R` |
