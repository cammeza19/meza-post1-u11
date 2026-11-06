package com.universidad.refactoringu11.inventario;

public class ClasificadorProducto {

    // Sin switch: delega el calculo en el polimorfismo de TipoProducto
    public double calcularRecargoEnvio(Producto producto) {
        return producto.getTipo().calcularRecargoEnvio(producto.getPrecio());
    }
}