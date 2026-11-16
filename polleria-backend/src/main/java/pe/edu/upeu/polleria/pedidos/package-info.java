/**
 * Pedidos solo consume los contratos de consulta y las excepciones compartidas.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
        "catalogo::producto-consulta", "clientes::cliente-consulta", "contenido::precios-promocion", "exception"
})
package pe.edu.upeu.polleria.pedidos;
