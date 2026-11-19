package com.irvin.casinovidas.vidas;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.vidas.item.HeartItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class VidasItems {
    private VidasItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CasinoVidasMod.MOD_ID);

    public static final DeferredHolder<Item, Item> HEART = ITEMS.register("heart",
            () -> new HeartItem(false, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, Item> GOLDEN_HEART = ITEMS.register("golden_heart",
            () -> new HeartItem(true, new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HEART.get());
            event.accept(GOLDEN_HEART.get());
        }
    }
}
