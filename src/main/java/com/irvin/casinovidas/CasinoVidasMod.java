package com.irvin.casinovidas;

import com.irvin.casinovidas.network.ModPayloads;
import com.irvin.casinovidas.slotmachine.Config;
import com.irvin.casinovidas.slotmachine.block.ModBlocks;
import com.irvin.casinovidas.slotmachine.block.ModItems;
import com.irvin.casinovidas.slotmachine.block.entity.ModBlockEntities;
import com.irvin.casinovidas.slotmachine.currency.CoinEvents;
import com.irvin.casinovidas.slotmachine.currency.CoinItems;
import com.irvin.casinovidas.slotmachine.screen.ModMenuTypes;
import com.irvin.casinovidas.slotmachine.sound.ModSounds;
import com.irvin.casinovidas.vidas.VidasItems;
import com.irvin.casinovidas.vidas.server.ServerEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Unico mod: sistema de vidas + tragamonedas con monedas.
 * Tanto el modid como el namespace de todos los items/bloques es "casinovidas".
 */
@Mod(CasinoVidasMod.MOD_ID)
public class CasinoVidasMod {
    public static final String MOD_ID = "casinovidas";

    public CasinoVidasMod(IEventBus modBus, ModContainer container) {
        // ---- Vidas ----
        VidasItems.ITEMS.register(modBus);
        modBus.addListener(VidasItems::addToCreativeTab);
        NeoForge.EVENT_BUS.register(new ServerEvents());

        // ---- Tragamonedas + monedas ----
        CoinItems.init();
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModSounds.register(modBus);
        modBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.register(new CoinEvents());
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // ---- Red (paquetes de los dos sistemas) ----
        modBus.addListener(ModPayloads::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModBlocks.SERVER_SLOT_MACHINE.get());
            event.accept(ModBlocks.DIGITIZER.get());
            event.accept(CoinItems.COIN.get());
        }
    }
}
