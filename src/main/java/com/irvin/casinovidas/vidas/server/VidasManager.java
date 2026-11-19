package com.irvin.casinovidas.vidas.server;

import com.irvin.casinovidas.vidas.VidasRules;
import com.irvin.casinovidas.vidas.network.SyncLivesPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Toda la logica de vidas. Solo se ejecuta en el servidor. */
public final class VidasManager {
    private VidasManager() {}

    /** Jugadores que murieron y deben ver el aviso cuando reaparezcan. */
    private static final Set<UUID> PENDING_NOTICE = new HashSet<>();

    private static VidasSavedData data(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(VidasSavedData.factory(), "vidas");
    }

    private static VidasSavedData.Entry entry(ServerPlayer p) {
        return data(p.server).get(p.getUUID());
    }

    public static int getLives(ServerPlayer p) {
        return entry(p).lives;
    }

    public static boolean canUse(boolean golden, int lives) {
        if (lives < 1) return false;
        return golden || lives < VidasRules.MAX_LIVES;
    }

    /** Sincroniza al cliente y marca el mundo como modificado. */
    private static void refresh(ServerPlayer p) {
        VidasSavedData.Entry e = entry(p);
        data(p.server).setDirty();
        PacketDistributor.sendToPlayer(p, new SyncLivesPayload(e.lives));
    }

    public static void onLogin(ServerPlayer p) {
        VidasSavedData.Entry e = entry(p);
        if (e.lives <= 0) p.setGameMode(GameType.SPECTATOR);
        refresh(p);
    }

    public static void onRespawn(ServerPlayer p) {
        VidasSavedData.Entry e = entry(p);
        if (e.lives <= 0) {
            p.setGameMode(GameType.SPECTATOR);
        } else if (PENDING_NOTICE.remove(p.getUUID())) {
            int lost = VidasRules.MAX_LIVES - Math.min(e.lives, VidasRules.MAX_LIVES);
            showTitle(p, Component.translatable("title.vidas.lost").withStyle(ChatFormatting.RED),
                    Component.translatable("subtitle.vidas.remaining", e.lives, lost).withStyle(ChatFormatting.GRAY));
        }
        refresh(p);
    }

    public static void onDeath(ServerPlayer p) {
        VidasSavedData.Entry e = entry(p);
        if (e.lives <= 0) return;
        if (e.lives >= VidasRules.GOLD_LIVES) {
            // pierde solo la vida extra del corazon dorado
            e.lives = VidasRules.MAX_LIVES;
            e.goldSeconds = 0;
            PENDING_NOTICE.add(p.getUUID());
        } else {
            e.lives--;
            if (e.lives <= 0) {
                e.goldSeconds = 0;
                p.server.getPlayerList().broadcastSystemMessage(
                        Component.translatable("message.vidas.eliminated", p.getDisplayName())
                                .withStyle(ChatFormatting.RED), false);
            } else {
                PENDING_NOTICE.add(p.getUUID());
            }
        }
        refresh(p);
    }

    /** Usa un corazon. Devuelve true si se aplico (y hay que gastar el item). */
    public static boolean consume(ServerPlayer p, boolean golden) {
        VidasSavedData.Entry e = entry(p);
        if (!canUse(golden, e.lives)) return false;
        if (!golden) {
            e.lives++;
            showTitle(p, Component.literal("\u2764").withStyle(ChatFormatting.RED),
                    Component.translatable("subtitle.vidas.healed").withStyle(ChatFormatting.GREEN));
        } else if (e.lives < VidasRules.MAX_LIVES) {
            e.lives++;
            showTitle(p, Component.literal("\u2764").withStyle(ChatFormatting.RED),
                    Component.translatable("subtitle.vidas.healed").withStyle(ChatFormatting.GREEN));
        } else {
            e.lives = VidasRules.GOLD_LIVES;
            e.goldSeconds = VidasRules.GOLD_SECONDS;
            showTitle(p, Component.translatable("title.vidas.golden").withStyle(ChatFormatting.GOLD),
                    Component.translatable("subtitle.vidas.golden", VidasRules.GOLD_SECONDS / 60).withStyle(ChatFormatting.YELLOW));
        }
        refresh(p);
        return true;
    }

    /** Comando de admin. */
    public static void setLives(ServerPlayer p, int lives) {
        VidasSavedData.Entry e = entry(p);
        e.lives = Math.max(0, Math.min(lives, VidasRules.GOLD_LIVES));
        e.goldSeconds = e.lives >= VidasRules.GOLD_LIVES ? VidasRules.GOLD_SECONDS : 0;
        if (e.lives > 0 && p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            p.setGameMode(GameType.SURVIVAL);
        } else if (e.lives <= 0) {
            p.setGameMode(GameType.SPECTATOR);
        }
        refresh(p);
    }

    /** Se llama cada tick del servidor; solo trabaja una vez por segundo. */
    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % 20 != 0) return;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            VidasSavedData.Entry e = data(server).get(p.getUUID());
            if (e.goldSeconds <= 0) continue;
            e.goldSeconds--;
            if (e.goldSeconds <= 0 && e.lives >= VidasRules.GOLD_LIVES) {
                e.lives = VidasRules.MAX_LIVES;
                p.displayClientMessage(Component.translatable("message.vidas.golden_faded")
                        .withStyle(ChatFormatting.GOLD), true);
                refresh(p);
            } else {
                data(server).setDirty();
            }
        }
    }

    private static void showTitle(ServerPlayer p, Component title, Component subtitle) {
        p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 80, 20));
        p.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        p.connection.send(new ClientboundSetTitleTextPacket(title));
    }
}
