package com.irvin.casinovidas.vidas.server;

import com.irvin.casinovidas.vidas.VidasRules;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ServerEvents {

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) VidasManager.onLogin(sp);
    }

    @SubscribeEvent
    public void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) VidasManager.onRespawn(sp);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) VidasManager.onDeath(sp);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post e) {
        VidasManager.tick(e.getServer());
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("vidas")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "jugador");
                                    int n = VidasManager.getLives(p);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal(p.getName().getString() + ": " + n + " vidas"), false);
                                    return n;
                                })))
                .then(Commands.literal("set")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .then(Commands.argument("vidas", IntegerArgumentType.integer(0, VidasRules.GOLD_LIVES))
                                        .executes(ctx -> {
                                            ServerPlayer p = EntityArgument.getPlayer(ctx, "jugador");
                                            int n = IntegerArgumentType.getInteger(ctx, "vidas");
                                            VidasManager.setLives(p, n);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal(p.getName().getString() + " ahora tiene " + n + " vidas"), true);
                                            return n;
                                        })))));
    }
}
