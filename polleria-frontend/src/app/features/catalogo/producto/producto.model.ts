export interface Presentacion { id?: number; nombre: string; precio: number; disponible: boolean }
import { CategoriaResumen } from '../categoria/categoria.model';
export interface ProductoRequest {
  presentaciones?: Presentacion[];
  nombre: string;
  descripcion: string | null;
  precio: number;
  disponible: boolean;
  categoriaId: number;
  imagenUrl: string | null;
}
export interface Producto extends Omit<ProductoRequest, 'categoriaId'> {
  id: number;
  eliminable?: boolean;
  categoria: CategoriaResumen;
}
