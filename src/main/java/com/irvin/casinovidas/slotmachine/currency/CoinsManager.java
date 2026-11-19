package com.irvin.casinovidas.slotmachine.currency;

import com.irvin.casinovidas.slotmachine.network.packet.BalanceS2CPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

/**
 * API de la moneda. Todo el balance se maneja en el servidor; el cliente solo recibe el numero.
 * El casino (y la tienda) deben usar SIEMPRE esta clase para cobrar y pagar.
 */
public final class CoinsManager {
    private CoinsManager() {}

    private static CoinsSavedData data(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(CoinsSavedData.factory(), "casino_coins");
    }

    public static long get(MinecraftServer server, UUID id) {
        return data(server).get(id);
    }

    public static void set(MinecraftServer server, UUID id, long value) {
        data(server).set(id, value);
        sync(server, id);
    }

    public static void add(MinecraftServer server, UUID id, long amount) {
        if (amount <= 0) return;
        data(server).set(id, data(server).get(id) + amount);
        sync(server, id);
    }

    /** Cobra {@code amount}. Devuelve false (sin cobrar nada) si no alcanza el balance. */
    public static boolean tryRemove(MinecraftServer server, UUID id, long amount) {
        CoinsSavedData d = data(server);
        long balance = d.get(id);
        if (amount < 0 || balance < amount) return false;
        d.set(id, balance - amount);
        sync(server, id);
        return true;
    }

    public static void sync(MinecraftServer server, UUID id) {
        ServerPlayer p = server.getPlayerList().getPlayer(id);
        if (p != null) sync(p);
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new BalanceS2CPayload(get(player.server, player.getUUID())));
    }
}
