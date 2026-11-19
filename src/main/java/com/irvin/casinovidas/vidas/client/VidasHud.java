package com.irvin.casinovidas.vidas.client;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.vidas.VidasRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** HUD: corazones en el costado derecho. Rojos = vidas que tienes, grises = perdidas, dorado = 4ta vida. */
@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class VidasHud {
    private VidasHud() {}

    private static final ResourceLocation FULL = ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "textures/gui/heart_full.png");
    private static final ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "textures/gui/heart_empty.png");
    private static final ResourceLocation GOLD = ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "textures/gui/heart_gold.png");

    private static final int SIZE = 12;   // tamano en pantalla (la textura es 16x16)
    private static final int GAP = 3;
    private static final int MARGIN = 6;

    @SubscribeEvent
    public static void onRegister(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "lives"),
                (g, deltaTracker) -> render(g, g.guiWidth(), g.guiHeight()));
    }

    private static void render(GuiGraphics g, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        int lives = ClientVidasData.getLives();
        boolean golden = lives >= VidasRules.GOLD_LIVES;
        int slots = golden ? VidasRules.GOLD_LIVES : VidasRules.MAX_LIVES;

        int x = width - SIZE - MARGIN;
        int total = slots * SIZE + (slots - 1) * GAP;
        int y0 = (height - total) / 2;

        for (int i = 0; i < slots; i++) {
            ResourceLocation tex;
            if (i >= VidasRules.MAX_LIVES) {
                tex = GOLD;
            } else {
                tex = i < Math.min(lives, VidasRules.MAX_LIVES) ? FULL : EMPTY;
            }
            int y = y0 + i * (SIZE + GAP);
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(SIZE / 16f, SIZE / 16f, 1f);
            g.blit(tex, 0, 0, 0f, 0f, 16, 16, 16, 16);
            g.pose().popPose();
        }
    }
}
