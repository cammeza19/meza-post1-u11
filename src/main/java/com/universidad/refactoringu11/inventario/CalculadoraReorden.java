package com.universidad.refactoringu11.inventario;

public class CalculadoraReorden {

    public static boolean necesitaReorden(int stockActual, int stockMinimo) {
        return stockActual < stockMinimo;
    }

    public static int calcularCantidad(int stockActual, int stockMinimo) {
        if (!necesitaReorden(stockActual, stockMinimo)) {
            return 0;
        }
        return (stockMinimo * 3) - stockActual;
    }
}
