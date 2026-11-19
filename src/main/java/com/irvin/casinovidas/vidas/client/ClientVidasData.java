package com.irvin.casinovidas.vidas.client;

import com.irvin.casinovidas.vidas.VidasRules;

/** Unico estado del lado cliente: el numero de vidas que manda el servidor. */
public final class ClientVidasData {
    private ClientVidasData() {}

    private static volatile int lives = VidasRules.MAX_LIVES;

    public static int getLives() {
        return lives;
    }

    public static void setLives(int value) {
        lives = value;
    }
}
