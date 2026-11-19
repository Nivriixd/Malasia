package com.irvin.casinovidas.slotmachine.currency;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CoinEvents {

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) CoinsManager.sync(sp);
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("monedas")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "jugador");
                                    long n = CoinsManager.get(p.server, p.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            p.getName().getString() + ": " + n + " monedas"), false);
                                    return (int) Math.min(n, Integer.MAX_VALUE);
                                })))
                .then(op("add"))
                .then(op("remove"))
                .then(op("set")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> op(String name) {
        return Commands.literal(name)
                .then(Commands.argument("jugador", EntityArgument.player())
                        .then(Commands.argument("cantidad", LongArgumentType.longArg(0))
                                .executes(ctx -> {
                                    ServerPlayer p = EntityArgument.getPlayer(ctx, "jugador");
                                    long n = LongArgumentType.getLong(ctx, "cantidad");
                                    long cur = CoinsManager.get(p.server, p.getUUID());
                                    switch (name) {
                                        case "add" -> CoinsManager.add(p.server, p.getUUID(), n);
                                        case "remove" -> CoinsManager.set(p.server, p.getUUID(), Math.max(0, cur - n));
                                        default -> CoinsManager.set(p.server, p.getUUID(), n);
                                    }
                                    long now = CoinsManager.get(p.server, p.getUUID());
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            p.getName().getString() + " ahora tiene " + now + " monedas"), true);
                                    return 1;
                                })));
    }
}
