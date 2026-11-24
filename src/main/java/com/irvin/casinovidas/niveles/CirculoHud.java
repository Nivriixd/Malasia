package com.irvin.casinovidas.niveles;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** Círculo en el centro: cambia de color un rato y se queda en el color final. */
@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CirculoHud {
    private CirculoHud() {}

    private static final int[] FINAL = {0xFF2ECC40, 0xFFFFDC00, 0xFFFF3B30}; // verde, amarillo, rojo
    private static final int CYCLE_MS = 2500;  // cuánto tiempo cambia de color
    private static final int HOLD_MS = 1500;   // cuánto se queda en el color final
    private static final int RADIUS = 40;      // radio en pixeles de GUI

    @SubscribeEvent
    public static void onRegister(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "circulo"),
                (g, deltaTracker) -> render(g));
    }

    private static void render(GuiGraphics g) {
        int c = ClientCirculo.color();
        if (c < 0 || Minecraft.getInstance().player == null) return;

        long t = ClientCirculo.elapsedMs();
        if (t > CYCLE_MS + HOLD_MS) {
            ClientCirculo.stop();
            return;
        }

        int argb;
        if (t < CYCLE_MS) {
            float hue = (t % 700) / 700f;
            argb = 0xFF000000 | Mth.hsvToRgb(hue, 1f, 1f);
        } else {
            argb = FINAL[Math.min(c, FINAL.length - 1)];
        }

        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        for (int dy = -RADIUS; dy <= RADIUS; dy++) {
            int dx = (int) Math.sqrt((double) RADIUS * RADIUS - (double) dy * dy);
            g.fill(cx - dx, cy + dy, cx + dx, cy + dy + 1, argb);
        }
    }
}
