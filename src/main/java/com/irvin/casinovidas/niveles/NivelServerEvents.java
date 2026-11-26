package com.irvin.casinovidas.niveles;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.Set;

public class NivelServerEvents {

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post e) {
        NivelManager.tick(e.getServer());
    }

    @SubscribeEvent
    public void onStopped(ServerStoppedEvent e) {
        NivelManager.clearPending();
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("nivel")
                .requires(src -> src.hasPermission(2));

        // /nivel verde cambio1, /nivel amarillo cambio2, ...
        for (NivelColor c : NivelColor.values()) {
            LiteralArgumentBuilder<CommandSourceStack> colorNode = Commands.literal(c.id);
            for (NivelEvents.Def d : NivelEvents.of(c)) {
                colorNode.then(Commands.literal("cambio" + d.numero())
                        .executes(ctx -> run(ctx.getSource(), d)));
            }
            root.then(colorNode);
        }

        root.then(Commands.literal("lista").executes(ctx -> lista(ctx.getSource())));
        e.getDispatcher().register(root);
    }

    private static int run(CommandSourceStack src, NivelEvents.Def d) {
        if (!NivelManager.activate(src.getServer(), d)) {
            src.sendFailure(Component.literal("Ese cambio ya está activo o en camino"));
            return 0;
        }
        return 1;
    }

    private static int lista(CommandSourceStack src) {
        Set<String> act = NivelManager.active(src.getServer());
        if (act.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No hay cambios activos"), false);
            return 0;
        }
        for (NivelEvents.Def d : NivelEvents.ALL) {
            if (act.contains(d.key())) {
                src.sendSuccess(() -> Component.literal("[" + d.color().id + " cambio" + d.numero() + "] " + d.titulo()), false);
            }
        }
        return act.size();
    }
}
