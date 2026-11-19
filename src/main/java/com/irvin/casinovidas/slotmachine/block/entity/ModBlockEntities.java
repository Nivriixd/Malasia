package com.irvin.casinovidas.slotmachine.block.entity;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CasinoVidasMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ServerSlotMachineBlockEntity>> SERVER_SLOT_MACHINE_BE =
            BLOCK_ENTITIES.register("server_slot_machine_be",
                    () -> BlockEntityType.Builder.of(ServerSlotMachineBlockEntity::new, ModBlocks.SERVER_SLOT_MACHINE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
