package com.universidad.refactoringu11.inventario;

public class EtiquetadoProducto {

    // Sin switch: delega la etiqueta en el polimorfismo de TipoProducto
    public String generarEtiqueta(Producto producto) {
        return producto.getTipo().formatearEtiqueta(producto.getNombre());
    }
}