package com.irvin.casinovidas.niveles;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
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
        NivelEstructuras.tick(e.getServer());
    }

    @SubscribeEvent
    public void onStopped(ServerStoppedEvent e) {
        NivelManager.clearPending();
        NivelEstructuras.limpiar();
    }

    /** Cambio amarillo 11: cancela una parte de los spawns naturales de animales. */
    private static final float ANIMALES_CANCELADOS = 0.75f; // 0.75 = 3 de cada 4 animales no aparecen

    @SubscribeEvent
    public void onSpawn(FinalizeSpawnEvent e) {
        if (e.getSpawnType() != MobSpawnType.NATURAL) return;
        if (e.getEntity().getType().getCategory() != MobCategory.CREATURE) return;
        if (!NivelManager.active(e.getLevel().getLevel().getServer()).contains("amarillo_11")) return;
        if (e.getLevel().getRandom().nextFloat() < ANIMALES_CANCELADOS) e.setSpawnCancelled(true);
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
        root.then(Commands.literal("estructura")
                .then(Commands.literal("lista").executes(ctx -> estructuraLista(ctx.getSource())))
                .then(Commands.literal("aparecer")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(
                                        c.getSource().registryAccess().registryOrThrow(Registries.STRUCTURE).keySet(), b))
                                .executes(ctx -> estructuraAparecer(ctx.getSource(),
                                        ResourceLocationArgument.getId(ctx, "id")))))
                .then(Commands.literal("borrar")
                        .then(Commands.literal("todas").executes(ctx -> estructuraBorrarTodas(ctx.getSource())))
                        .then(Commands.argument("n", IntegerArgumentType.integer(1))
                                .executes(ctx -> estructuraBorrar(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "n"))))));

        root.then(Commands.literal("base")
                .then(Commands.literal("agregar").executes(ctx -> baseAgregar(ctx.getSource())))
                .then(Commands.literal("lista").executes(ctx -> baseLista(ctx.getSource())))
                .then(Commands.literal("limpiar").executes(ctx -> baseLimpiar(ctx.getSource()))));
        root.then(Commands.literal("lista").executes(ctx -> lista(ctx.getSource())));
        e.getDispatcher().register(root);
    }

        @SubscribeEvent
    public void onTravel(EntityTravelToDimensionEvent e) {
        if (!(e.getEntity().level() instanceof ServerLevel level)) return;
        String dim = e.getDimension().location().toString();
        if (NivelDims.isOpen(level.getServer(), dim)) return;

        e.setCanceled(true);
        if (e.getEntity() instanceof ServerPlayer p) {
            p.displayClientMessage(Component.literal("Esta dimensión está cerrada por ahora"), true);
        }
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
