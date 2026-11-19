package com.irvin.casinovidas.client;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.screen.ModMenuTypes;
import com.irvin.casinovidas.slotmachine.screen.ServerSlotMachineScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Registro de pantallas (solo cliente). */
@EventBusSubscriber(modid = CasinoVidasMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SERVER_SLOT_MACHINE_MENU.get(), ServerSlotMachineScreen::new);
    }
}
