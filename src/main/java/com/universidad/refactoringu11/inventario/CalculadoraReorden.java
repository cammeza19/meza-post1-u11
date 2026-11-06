package com.universidad.refactoringu11.inventario;

public final class CalculadoraReorden {

    private static final int FACTOR_REORDEN = 3;

    private CalculadoraReorden() {
        // Clase utilitaria: no se instancia
    }

    public static boolean necesitaReorden(int stockActual, int stockMinimo) {
        return stockActual < stockMinimo;
    }

    public static int calcularCantidad(int stockActual, int stockMinimo) {
        if (!necesitaReorden(stockActual, stockMinimo)) {
            return 0;
        }
        return (stockMinimo * FACTOR_REORDEN) - stockActual;
    }
}