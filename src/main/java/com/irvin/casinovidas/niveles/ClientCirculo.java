package com.irvin.casinovidas.niveles;

public final class ClientCirculo {
    private ClientCirculo() {}

    private static volatile int color = -1;
    private static volatile long startMs;

    public static void show(int c) {
        startMs = System.currentTimeMillis();
        color = c;
    }

    public static void stop() { color = -1; }
    public static int color() { return color; }
    public static long elapsedMs() { return System.currentTimeMillis() - startMs; }
}
