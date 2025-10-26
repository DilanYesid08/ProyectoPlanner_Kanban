package com.proyecto.kanban.util;

import java.util.Scanner;

/**
 * Utilidad simple para interacción por consola.
 *
 * Proporciona métodos estáticos para leer entradas básicas y mantener el
 * flujo de la aplicación de consola simple y repetible.
 *
 * Nota sobre {@code Scanner} y lectura:
 * - Usamos un único {@code Scanner} estático para no cerrar la entrada estándar
 *   accidentalmente (cerrar System.in puede causar problemas en la consola).
 * - El método {@link #readInt(String)} intenta parsear la entrada y entra en
 *   un pequeño bucle hasta que el usuario ingresa un número válido.
 */
public class ConsoleUtil {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt + ": ");
        return SCANNER.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                String line = readLine(prompt);
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Intenta de nuevo.");
            }
        }
    }

    public static void pause() {
        System.out.println("Presiona Enter para continuar...");
        SCANNER.nextLine();
    }
}
