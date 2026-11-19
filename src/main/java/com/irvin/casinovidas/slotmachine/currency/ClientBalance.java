package com.irvin.casinovidas.slotmachine.currency;

/** Unico estado del cliente: el balance que manda el servidor. */
public final class ClientBalance {
    private ClientBalance() {}

    private static volatile long balance = 0L;

    public static long get() {
        return balance;
    }

    public static void set(long value) {
        balance = value;
    }
}
