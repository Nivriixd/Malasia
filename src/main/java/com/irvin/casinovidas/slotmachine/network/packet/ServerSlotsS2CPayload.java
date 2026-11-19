package com.irvin.casinovidas.slotmachine.network.packet;

import com.irvin.casinovidas.CasinoVidasMod;
import com.irvin.casinovidas.slotmachine.screen.ServerSlotMachineScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Servidor -> cliente: resultado de los rodillos. */
public record ServerSlotsS2CPayload(BlockPos pos, int[] results) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerSlotsS2CPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "slots_s2c"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerSlotsS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos());
                buf.writeVarIntArray(p.results());
            },
            buf -> new ServerSlotsS2CPayload(buf.readBlockPos(), buf.readVarIntArray()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerSlotsS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof ServerSlotMachineScreen sss) {
                sss.updateResults(payload.results());
            }
        });
    }
}
