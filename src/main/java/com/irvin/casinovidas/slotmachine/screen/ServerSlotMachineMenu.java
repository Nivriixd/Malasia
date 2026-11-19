package com.irvin.casinovidas.slotmachine.screen;

import com.irvin.casinovidas.slotmachine.Config;
import com.irvin.casinovidas.slotmachine.block.ModBlocks;
import com.irvin.casinovidas.slotmachine.block.entity.ServerSlotMachineBlockEntity;
import com.irvin.casinovidas.slotmachine.currency.CoinItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.List;

public class ServerSlotMachineMenu extends AbstractContainerMenu {

    // Antes era "static": con 2 jugadores en maquinas distintas se pisaban. Ahora es por menu.
    private final ServerSlotMachineBlockEntity blockEntity;

    private final Level level;
    private final ContainerData data;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 0; // sin slots: se apuesta con el balance

    // Constructor del lado cliente (lo llama NeoForge con los datos extra que escribio openMenu)
    public ServerSlotMachineMenu(int pContainerId, Inventory inv, RegistryFriendlyByteBuf extraData) {
        this(pContainerId, inv,
                inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(ServerSlotMachineBlockEntity.DATA_COUNT));
    }

    public ServerSlotMachineMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.SERVER_SLOT_MACHINE_MENU.get(), pContainerId);
        this.blockEntity = (ServerSlotMachineBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        // Sin slots de items: la apuesta se cobra del balance de monedas.

        addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    /** Returns the bet item configured for this server slot machine. */
    public ItemStack getDisplayBetItem() {
        return new ItemStack(CoinItems.COIN.get());
    }

    /** Returns the item prizes (3 iguales) for display in the prizes panel. */
    public List<Config.ServerPrize> getServerPrizes() {
        if (Config.serverPrizes == null) return Collections.emptyList();
        return Config.serverPrizes;
    }

    public int getServiceState() {
        return blockEntity != null ? blockEntity.getServiceState() : ServerSlotMachineBlockEntity.SERVICE_NOT_CONFIGURED;
    }

    public boolean hasOutputItem() {
        return blockEntity != null && blockEntity.hasOutputItem();
    }

    public boolean isSpinning() {
        return data.get(0) == 0;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX,
                    TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, ModBlocks.SERVER_SLOT_MACHINE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
}
