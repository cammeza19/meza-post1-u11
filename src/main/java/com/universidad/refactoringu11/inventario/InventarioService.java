package com.universidad.refactoringu11.inventario;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class InventarioService {

    private final List<Producto> productos = new ArrayList<>();
    private final List<String> movimientos = new ArrayList<>();

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    // Metodo principal refactorizado: orquesta delegando en metodos intencionales (<= 10 lineas)
    public String actualizarInventario(Long productoId, int cantidad, String tipoMovimiento) {
        if (esEntradaInvalida(productoId, cantidad, tipoMovimiento)) return "ERROR_DATOS_INVALIDOS";
        Producto producto = buscarProducto(productoId);
        if (producto == null) return "ERROR_PRODUCTO_NO_EXISTE";
        if (esStockInsuficiente(producto, cantidad, tipoMovimiento)) return "ERROR_STOCK_INSUFICIENTE";

        int nuevoStock = aplicarMovimiento(producto, cantidad, tipoMovimiento);
        registrarAuditoria(tipoMovimiento, cantidad, producto, nuevoStock);
        return procesarAlertaOResultado(producto, nuevoStock);
    }

    private boolean esEntradaInvalida(Long productoId, int cantidad, String tipoMovimiento) {
        return productoId == null || cantidad <= 0
                || (!"ENTRADA".equals(tipoMovimiento) && !"SALIDA".equals(tipoMovimiento));
    }

    private Producto buscarProducto(Long productoId) {
        for (Producto p : productos) {
            if (p.getId().equals(productoId)) return p;
        }
        return null;
    }

    private boolean esStockInsuficiente(Producto producto, int cantidad, String tipoMovimiento) {
        return "SALIDA".equals(tipoMovimiento) && (producto.getStock() - cantidad < 0);
    }

    private int aplicarMovimiento(Producto producto, int cantidad, String tipoMovimiento) {
        int nuevoStock = "ENTRADA".equals(tipoMovimiento)
                ? producto.getStock() + cantidad
                : producto.getStock() - cantidad;
        producto.setStock(nuevoStock);
        return nuevoStock;
    }

    private void registrarAuditoria(String tipoMovimiento, int cantidad, Producto producto, int nuevoStock) {
        String registro = tipoMovimiento + " de " + cantidad + " unidades de "
                + producto.getNombre() + " -> stock final: " + nuevoStock;
        movimientos.add(registro);
        System.out.println(registro);
    }

    private String procesarAlertaOResultado(Producto producto, int nuevoStock) {
        if (CalculadoraReorden.necesitaReorden(nuevoStock, producto.getStockMinimo())) {
            int cantidadReorden = CalculadoraReorden.calcularCantidad(nuevoStock, producto.getStockMinimo());
            System.out.println("ALERTA: " + producto.getNombre()
                    + " bajo stock minimo. Reordenar " + cantidadReorden + " unidades.");
            return "OK_BAJO_STOCK_REORDEN_" + cantidadReorden;
        }
        return "OK_" + nuevoStock;
    }
}