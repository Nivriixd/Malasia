package com.irvin.casinovidas.slotmachine.currency;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/** Balance arriba a la derecha. Cambia HUD_Y si choca con los iconos de efectos. */
@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CoinHud {
    private CoinHud() {}

    private static final int HUD_Y = 4;
    private static final int MARGIN = 6;

    @SubscribeEvent
    public static void onRegister(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "balance"),
                (g, deltaTracker) -> render(g, g.guiWidth()));
    }

    private static void render(GuiGraphics g, int width) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;

        String text = String.format("%,d", ClientBalance.get());
        int textWidth = mc.font.width(text);
        int iconX = width - 16 - MARGIN;

        g.renderItem(new ItemStack(CoinItems.COIN.get()), iconX, HUD_Y);
        g.drawString(mc.font, text, iconX - 3 - textWidth, HUD_Y + 4, 0xFFD700, true);
    }
}
