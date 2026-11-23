package com.irvin.casinovidas;

import com.irvin.casinovidas.network.ModPayloads;
import com.irvin.casinovidas.slotmachine.block.ModBlocks;
import com.irvin.casinovidas.slotmachine.block.ModItems;
import com.irvin.casinovidas.slotmachine.currency.CoinEvents;
import com.irvin.casinovidas.slotmachine.currency.CoinItems;
import com.irvin.casinovidas.vidas.VidasItems;
import com.irvin.casinovidas.vidas.server.ServerEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(CasinoVidasMod.MOD_ID)
public class CasinoVidasMod {
    public static final String MOD_ID = "casinovidas";

    public CasinoVidasMod(IEventBus modBus, ModContainer container) {

        VidasItems.ITEMS.register(modBus);
        modBus.addListener(VidasItems::addToCreativeTab);
        NeoForge.EVENT_BUS.register(new ServerEvents());

        CoinItems.init();
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        modBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.register(new CoinEvents());

        modBus.addListener(ModPayloads::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModBlocks.DIGITIZER.get());
            event.accept(CoinItems.COIN.get());
        }
    }
}
