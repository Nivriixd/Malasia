package com.irvin.casinovidas.slotmachine.network.packet;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.block.entity.ServerSlotMachineBlockEntity;
import com.irvin.casinovidas.slotmachine.screen.ServerSlotMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Cliente -> servidor: la animacion de los rodillos termino. El servidor entrega el premio pendiente.
 */
public record ServerClaimPayoutC2SPayload(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerClaimPayoutC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "claim_payout"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerClaimPayoutC2SPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, ServerClaimPayoutC2SPayload::pos, ServerClaimPayoutC2SPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerClaimPayoutC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            // Solo vale si el jugador tiene abierta ESA maquina (evita paquetes falsos a otras posiciones)
            if (!(player.containerMenu instanceof ServerSlotMachineMenu menu) || !menu.getBlockPos().equals(payload.pos())) return;
            BlockEntity be = player.serverLevel().getBlockEntity(payload.pos());
            if (be instanceof ServerSlotMachineBlockEntity slotMachine) {
                slotMachine.releasePayout();
            }
        });
    }
}
