package com.irvin.casinovidas.niveles;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, value = Dist.CLIENT)
public final class FuerzaKeyTick {
    private FuerzaKeyTick() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) return;
        while (FuerzaKeys.TOGGLE.consumeClick()) {
            PacketDistributor.sendToServer(new FuerzaToggleC2SPayload());
        }
    }
}
