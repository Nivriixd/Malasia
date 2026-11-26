package com.irvin.casinovidas.niveles;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class FuerzaKeys {
    private FuerzaKeys() {}

    public static final KeyMapping TOGGLE =
            new KeyMapping("key.casinovidas.fuerza", GLFW.GLFW_KEY_G, "key.categories.casinovidas");

    @SubscribeEvent
    public static void onRegister(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
    }
}
