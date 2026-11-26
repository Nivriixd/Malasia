package com.irvin.casinovidas.niveles;

import com.irvin.casinovidas.CasinoVidasMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Cliente -> servidor: el jugador pulsó la tecla de fuerza. */
public record FuerzaToggleC2SPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FuerzaToggleC2SPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CasinoVidasMod.MOD_ID, "fuerza_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FuerzaToggleC2SPayload> STREAM_CODEC =
            StreamCodec.unit(new FuerzaToggleC2SPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FuerzaToggleC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sp) NivelFuerza.toggle(sp);
        });
    }
}
