package com.irvin.casinovidas.slotmachine.network.packet;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.block.entity.ServerSlotMachineBlockEntity;
import com.irvin.casinovidas.slotmachine.screen.ServerSlotMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Cliente -> servidor: iniciar (true) o parar (false) el giro. */
public record ServerSlotsC2SPayload(BlockPos pos, boolean spinState) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerSlotsC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "slots_c2s"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSlotsC2SPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerSlotsC2SPayload::pos,
                    ByteBufCodecs.BOOL, ServerSlotsC2SPayload::spinState,
                    ServerSlotsC2SPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerSlotsC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            // Solo vale si el jugador tiene abierta ESA maquina
            if (!(player.containerMenu instanceof ServerSlotMachineMenu menu) || !menu.getBlockPos().equals(payload.pos())) return;
            BlockEntity be = player.serverLevel().getBlockEntity(payload.pos());
            if (be instanceof ServerSlotMachineBlockEntity ssbe) {
                int[] results = payload.spinState() ? ssbe.startSpin(player) : ssbe.stopSpin(player);
                PacketDistributor.sendToPlayer(player, new ServerSlotsS2CPayload(payload.pos(), results));
            }
        });
    }
}
