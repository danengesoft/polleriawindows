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
