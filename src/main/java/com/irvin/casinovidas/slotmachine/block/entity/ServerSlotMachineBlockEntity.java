package com.irvin.casinovidas.slotmachine.block.entity;

import com.irvin.casinovidas.slotmachine.Config;
import com.irvin.casinovidas.slotmachine.currency.CoinsManager;
import com.irvin.casinovidas.slotmachine.screen.ServerSlotMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

/**
 * Tragamonedas del servidor con monedas (balance). La apuesta se cobra del balance del jugador.
 * 3 iguales: premio de ITEM al azar (Config.serverPrizes), entregado al terminar la animacion.
 * 2 iguales: premio de monedas (Config.serverCoinPrizes), acreditado al balance. No hay slots de items.
 */
public class ServerSlotMachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int DATA_COUNT = 1;

    public static final int SERVICE_READY = 0;
    public static final int SERVICE_NOT_CONFIGURED = 1;

    public static final int RESULT_SPINNING       = 0;
    public static final int RESULT_INVALID_BET    = 6;   // ahora significa: monedas insuficientes / giro invalido
    public static final int RESULT_NOT_CONFIGURED = 9;
    public static final int RESULT_TAKE_OUT_WIN   = 10;  // sin uso (se conserva por compatibilidad con la pantalla)

    /** Ticks que se espera al cliente antes de acreditar un premio pendiente por su cuenta. */
    private static final int PAYOUT_TIMEOUT_TICKS = 200;

    private int stopped = 1;
    private UUID spinner = null;

    private int pendingCoins = 0;
    private ItemStack pendingItem = ItemStack.EMPTY;
    private UUID pendingOwner = null;
    private int pendingAge = 0;

    protected final ContainerData data;

    public ServerSlotMachineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.SERVER_SLOT_MACHINE_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? ServerSlotMachineBlockEntity.this.stopped : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) ServerSlotMachineBlockEntity.this.stopped = value;
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public ContainerData getData() {
        return data;
    }

    public boolean isConfigured() {
        List<Config.CoinPrize> coins = Config.serverCoinPrizes;
        List<Config.ServerPrize> items = Config.serverPrizes;
        return Config.serverBetCost > 0
                && ((coins != null && !coins.isEmpty()) || (items != null && !items.isEmpty()));
    }

    public int getServiceState() {
        return isConfigured() ? SERVICE_READY : SERVICE_NOT_CONFIGURED;
    }

    /** Ya no hay slot de salida. */
    public boolean hasOutputItem() {
        return false;
    }

    /**
     * Entrega el premio pendiente: monedas al balance e item al inventario del jugador
     * (si no cabe, cae al suelo junto a el; si esta desconectado, cae en la maquina).
     * Seguro de llamar varias veces.
     */
    public void releasePayout() {
        if (pendingOwner == null || (pendingCoins <= 0 && pendingItem.isEmpty())
                || level == null || level.isClientSide()) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (server == null) return;

        int coins = pendingCoins;
        ItemStack item = pendingItem;
        UUID owner = pendingOwner;
        pendingCoins = 0;
        pendingItem = ItemStack.EMPTY;
        pendingOwner = null;
        pendingAge = 0;

        ServerPlayer p = server.getPlayerList().getPlayer(owner);

        if (coins > 0) {
            CoinsManager.add(server, owner, coins);
            if (p != null) {
                p.displayClientMessage(Component.translatable("message.slotmachinemod.coins_won", coins), true);
            }
        }
        if (!item.isEmpty()) {
            Component name = item.getHoverName();
            int count = item.getCount();
            if (p != null) {
                ItemHandlerHelper.giveItemToPlayer(p, item);
                p.displayClientMessage(Component.translatable("message.slotmachinemod.item_won", count, name), true);
            } else {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                        worldPosition.getZ() + 0.5, item);
            }
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.casinovidas.server_slot_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        releasePayout(); // si reabre la pantalla a mitad de animacion, se acredita lo pendiente
        return new ServerSlotMachineMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    // 1.21: guardar/cargar reciben HolderLookup.Provider (hace falta para serializar ItemStacks)
    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        pTag.putInt("pendingCoins", pendingCoins);
        if (!pendingItem.isEmpty()) pTag.put("pendingItem", pendingItem.save(registries));
        if (pendingOwner != null) pTag.putUUID("pendingOwner", pendingOwner);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.loadAdditional(pTag, registries);
        pendingCoins = pTag.getInt("pendingCoins");
        pendingItem = pTag.contains("pendingItem")
                ? ItemStack.parseOptional(registries, pTag.getCompound("pendingItem"))
                : ItemStack.EMPTY;
        pendingOwner = pTag.hasUUID("pendingOwner") ? pTag.getUUID("pendingOwner") : null;
        stopped = 1; // un giro a medias no sobrevive a un reinicio
        spinner = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) releasePayout();
        super.setRemoved();
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState, BlockEntity pBlockEntity) {
        if ((pendingCoins > 0 || !pendingItem.isEmpty()) && ++pendingAge >= PAYOUT_TIMEOUT_TICKS) {
            releasePayout();
        }
    }

    // ------------------------------------------------------------------ giro

    /** Cobra la apuesta del balance del jugador e inicia el giro. */
    public int[] startSpin(ServerPlayer player) {
        releasePayout(); // por si quedo algo pendiente de un giro anterior

        if (!isConfigured()) {
            stopped = 1;
            return blockedResult(RESULT_NOT_CONFIGURED);
        }

        if (!CoinsManager.tryRemove(player.server, player.getUUID(), Config.serverBetCost)) {
            stopped = 1;
            return blockedResult(RESULT_INVALID_BET);
        }

        spinner = player.getUUID();
        stopped = 0;
        return new int[]{RESULT_SPINNING, RESULT_SPINNING, RESULT_SPINNING};
    }

    /**
     * Termina el giro. Solo vale si el jugador pago un giro en marcha: un paquete "stop" sin haber
     * pagado se rechaza (si no, se podrian conseguir premios gratis).
     */
    public int[] stopSpin(ServerPlayer player) {
        if (stopped != 0 || spinner == null || !spinner.equals(player.getUUID())) {
            return blockedResult(RESULT_INVALID_BET);
        }
        UUID owner = spinner;
        spinner = null;
        stopped = 1;

        double roll = Math.random();
        boolean tripleMatch = roll < Config.serverTripleWinChance;
        boolean doubleMatch = !tripleMatch && roll < Config.serverTripleWinChance + Config.serverDoubleWinChance;

        if (tripleMatch) {
            // 3 iguales: item al azar entre los premios configurados
            int selected = pickWeighted(Config.serverPrizes, Config.ServerPrize::chance);
            if (selected >= 0) {
                pendingItem = Config.serverPrizes.get(selected).toStack();
                pendingOwner = owner;
                pendingAge = 0;
                setChanged();
                return createCosmeticReels(true, true);
            }
            // Respaldo: no hay premios de item validos, se paga en monedas
            int coinSel = pickWeighted(Config.serverCoinPrizes, Config.CoinPrize::chance);
            if (coinSel >= 0) {
                int coins = Config.serverCoinPrizes.get(coinSel).coins();
                pendingCoins = Config.doubleTriplePayout ? coins * 2 : coins;
                pendingOwner = owner;
                pendingAge = 0;
                setChanged();
                return createCosmeticReels(true, true);
            }
        } else if (doubleMatch) {
            // 2 iguales: premio de consuelo en monedas
            int selected = pickWeighted(Config.serverCoinPrizes, Config.CoinPrize::chance);
            if (selected >= 0) {
                pendingCoins = Config.serverCoinPrizes.get(selected).coins();
                pendingOwner = owner;
                pendingAge = 0;
                setChanged();
                return createCosmeticReels(true, false);
            }
        }
        return createCosmeticReels(false, false);
    }

    /** Elige un indice al azar segun el peso de cada elemento; -1 si la lista esta vacia o sin pesos. */
    private static <T> int pickWeighted(List<T> list, ToDoubleFunction<T> weight) {
        if (list == null || list.isEmpty()) return -1;

        double total = 0;
        for (T t : list) total += weight.applyAsDouble(t);
        if (total <= 0) return -1;

        double rand = Math.random() * total;
        double cumulative = 0;
        for (int i = 0; i < list.size(); i++) {
            cumulative += weight.applyAsDouble(list.get(i));
            if (rand < cumulative) return i;
        }
        return list.size() - 1;
    }

    private int[] createCosmeticReels(boolean anyWin, boolean tripleMatch) {
        if (tripleMatch) {
            int sym = (int) (Math.random() * 5 + 1);
            return new int[]{sym, sym, sym};
        } else if (anyWin) {
            int sym = (int) (Math.random() * 5 + 1);
            int other;
            do { other = (int) (Math.random() * 5 + 1); } while (other == sym);
            return new int[]{sym, sym, other};
        } else {
            int r1 = (int) (Math.random() * 5 + 1);
            int r2;
            do { r2 = (int) (Math.random() * 5 + 1); } while (r2 == r1);
            int r3;
            do { r3 = (int) (Math.random() * 5 + 1); } while (r3 == r1 || r3 == r2);
            return new int[]{r1, r2, r3};
        }
    }

    private int[] blockedResult(int result) {
        return new int[]{result, result, result};
    }

    /** Llamado al romper el bloque: acredita lo pendiente (no hay items que soltar). */
    public void drops() {
        releasePayout();
    }
}
