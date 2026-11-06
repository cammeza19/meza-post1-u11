package com.universidad.refactoringu11.reportes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.universidad.refactoringu11.inventario.InventarioService;
import com.universidad.refactoringu11.inventario.NotificadorBajoStock;
import com.universidad.refactoringu11.inventario.Producto;

@Service
public class ReporteBajoStockService implements NotificadorBajoStock {

    private final InventarioService inventarioService;
    private final List<String> alertas = new ArrayList<>();

    // @Lazy rompe el ciclo de creación de beans (InventarioService <-> ReporteBajoStockService)
    public ReporteBajoStockService(@Lazy InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @Override
    public void notificar(Producto producto, int cantidadReorden) {
        alertas.add("Alerta de bajo stock: " + producto.getNombre()
                + " requiere reorden de " + cantidadReorden + " unidades.");
    }

    public String generarReporte() {
        if (alertas.isEmpty()) {
            return "Sin alertas de bajo stock.";
        }
        return "Reporte de bajo stock (" + alertas.size() + " alertas):\n"
                + String.join("\n", alertas);
    }
}