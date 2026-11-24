package com.irvin.casinovidas.niveles;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Set;

public class NivelServerEvents {

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post e) {
        NivelManager.tick(e.getServer());
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("nivel")
                .requires(src -> src.hasPermission(2));

        for (NivelColor c : NivelColor.values()) {
            int max = NivelEvents.of(c).size();
            root.then(Commands.literal(c.id)
                    .then(Commands.argument("numero", IntegerArgumentType.integer(1, max))
                            .executes(ctx -> run(ctx.getSource(), c, IntegerArgumentType.getInteger(ctx, "numero")))));
        }

        root.then(Commands.literal("lista").executes(ctx -> lista(ctx.getSource())));
        root.then(Commands.literal("reset").executes(ctx -> {
            NivelManager.reset(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(() -> Component.literal("Eventos activos limpiados"), true);
            return 1;
        }));

        e.getDispatcher().register(root);
    }

    private static int run(CommandSourceStack src, NivelColor c, int n) {
        NivelEvents.Def d = NivelEvents.find(c, n);
        if (d == null) {
            src.sendFailure(Component.literal("Ese evento no existe"));
            return 0;
        }
        if (!NivelManager.activate(src.getServer(), d)) {
            src.sendFailure(Component.literal("Ese evento ya está activo"));
            return 0;
        }
        return 1;
    }

    private static int lista(CommandSourceStack src) {
        Set<String> act = NivelManager.active(src.getServer());
        if (act.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No hay eventos activos"), false);
            return 0;
        }
        for (NivelEvents.Def d : NivelEvents.ALL) {
            if (act.contains(d.key())) {
                src.sendSuccess(() -> Component.literal("[" + d.color().id + " " + d.numero() + "] " + d.nombre()), false);
            }
        }
        return act.size();
    }
}
