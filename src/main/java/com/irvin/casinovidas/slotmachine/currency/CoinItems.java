package com.irvin.casinovidas.slotmachine.currency;

import com.irvin.casinovidas.slotmachine.block.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Item "moneda": un item normal que no hace nada por si solo (se puede guardar, tirar o regalar).
 * Solo se convierte en balance al usarlo (clic derecho) en el Digitalizador.
 */
public final class CoinItems {
    private CoinItems() {}

    public static final DeferredHolder<Item, Item> COIN = ModItems.ITEMS.register("coin",
            () -> new Item(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    /** Fuerza la carga de la clase para que el item se registre. */
    public static void init() {}
}
